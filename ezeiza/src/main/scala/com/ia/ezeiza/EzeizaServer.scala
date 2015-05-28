package com.ia.ezeiza

import akka.actor.ActorSystem
import com.typesafe.config.Config
import akka.io.IO
import org.apache.log4j.Logger
import spray.can.Http
import akka.actor.Props
import akka.actor.Actor
import spray.routing.HttpService
import spray.routing._
import spray.http._
import spray.util.LoggingContext
import spray.http.StatusCodes._
import spray.http.HttpHeaders._
import spray.http.HttpMethods._
import MediaTypes._
import spray.json._
import spray.json.DefaultJsonProtocol._
import spray.httpx.SprayJsonSupport._
import akka.util.Timeout
import java.text.SimpleDateFormat
import java.util.UUID


object EzeizaServer {

  val logger = Logger.getLogger(getClass)
  
  def run(actorSystem: ActorSystem, config: Config, dashboards: List[Dashboard], beginAt: java.util.Date, ordering: List[String]) = {
	    val interfaceExpression = config.getString("ezeizaActor.httpServer.interface")
	    val portNumber = config.getInt("ezeizaActor.httpServer.port")
	    logger.info("Starting ezeiza service at %s:%d".format(interfaceExpression, portNumber))
	    
		val httpService = actorSystem.actorOf(Props(new EzeizaServer(config, dashboards, dashboards.head.photogallery, beginAt, ordering)), "ezeiza-service")
		implicit val system = actorSystem
		IO(Http) ! Http.Bind(httpService, interface = interfaceExpression, port = portNumber)        
  }
  
}


object EzeizaJsonImplicits extends DefaultJsonProtocol {
//  implicit val impQuery = jsonFormat2(Query)
  
  implicit object DateJsonFormat extends RootJsonFormat[java.util.Date] {
    // Al ser usado desde un actor, no hay problema con que no sea thread safe
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")//DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss:SSS")
    
    def write(d: java.util.Date) = JsString(sdf.format(d))

    def read(value: JsValue) = value match {
      case JsString(dateString) => sdf.parse(dateString)// DateTime.fromIsoDateTimeString(isoDateTimeString).getOrElse(deserializationError("Expecting ISO DateTime string"))
      case _ => deserializationError("Expecting Date string")// "Expecting ISO DateTime string")
    }
  }  
//  implicit val impPrinterResults = jsonFormat10(PrinterResult)
//  implicit val impDashboardResults = jsonFormat2(DashboardResults)
  
  implicit val impServerTimestamp = jsonFormat1(ServerTimestamp)
  implicit val impCellPhase = jsonFormat2(CellPhase)
  implicit val impBoardConf = jsonFormat5(BoardConfiguration)

}


class EzeizaServer(config: Config, dashboards: List[Dashboard], photogallery: Photogallery, beginAt: java.util.Date, ordering: List[String]) extends Actor with HttpService {
  
  val configId = UUID.randomUUID().toString()
  
  def actorRefFactory = context

  import EzeizaJsonImplicits._
  
  val actorLogger = Logger.getLogger(getClass)

  val corsHeaders = List(`Access-Control-Allow-Origin`(AllOrigins),
    `Access-Control-Allow-Methods`(GET, POST, OPTIONS, DELETE),
    `Access-Control-Allow-Headers`("Origin, X-Requested-With, Content-Type, Accept, Accept-Encoding, Accept-Language, Host, Referer, User-Agent"))  

    
  def corsDirective: Directive0 = {
      val rh = implicitly[RejectionHandler]
      respondWithHeaders(corsHeaders) & handleRejections(rh)
  }    
   
  var startTime = beginAt
  
  var newId: Option[String] = None
  
  
  def receive = runRoute {
    
    corsDirective {
      path("") {
        get {
	        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
	          complete {
	            <html>
              <body>
                <h1>Ezeiza resources</h1>
        	  	<ul>
        	  		<li>/configuration/(x)/(y)</li>
        	  		<li>/cells</li>
        	  		<li>/image/(name)</li>
        	  		<li>/now</li>
        	  		<li>/restartAt/(HH:mm:ss)</li>
	        	    <li>/flush</li>
        	  	</ul>
              </body>
            </html>
	          }
	        }          
        }
      } ~
      pathPrefix("configuration" / IntNumber / IntNumber) { (y, x) => {
    	  get {
    	    try {
    	    	println("configuration %dx%d".format(y, x))
    	    	val phases = dashboards.map(dashboard => CellPhase(dashboard.cellContent(y, x).getName, dashboard.delay))
    	    	val cellOrdering = ordering.map(photogallery.portionName(_, x, y))
    	    	
    	    	val conf = BoardConfiguration(newId.getOrElse(configId), startTime.getTime, phases, cellOrdering, newId.map(_ => true).getOrElse(false))
    	    	
    	    	println(conf)
    	    	
    			complete(conf)
    	    }
    	    catch {
    	      case e: Exception => {
    	        e.printStackTrace()
    	        complete("No existe posicion (%d, %d)".format(y, x))
    	      }
    	    }
    	    
    	  }
      	} 
      } ~
      pathPrefix("image" / Segment) { imageName => {
    	  val file = photogallery.imageForName(imageName)
    	  if(!file.exists()) {
    	    println("Imagen %s en %s no existe".format(imageName, file.getAbsolutePath()))
    	  }
    	  getFromFile(file)
      	} 
      } ~
      path("now") {
        println("now")
        complete(ServerTimestamp(System.currentTimeMillis()))
      } ~
//      path("cells") {
//        val cells = dashboards.head.cellMap.keys.toList.sorted.mkString(", ")
//        complete(cells.toString)
//      } ~
      path("flush") {
        newId = Some(UUID.randomUUID().toString)
        complete("Flushed")
      } ~
      pathPrefix("restartAt" / Segment) { timeExpression => {
    	  get {
    	    val sdf = new SimpleDateFormat("HH:mm:ss")
    	    startTime = sdf.parse(timeExpression)
    	    complete("OK!")
    	  }
      	} 
      }       
    }
  }
  
}


case class ServerTimestamp(ts: Long)

case class BoardConfiguration(configId: String, startTime: Long, phases: List[CellPhase], ordering: List[String], flush: Boolean = false)

case class CellPhase(imageName: String, delay: Int)


