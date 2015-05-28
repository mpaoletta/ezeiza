package com.ia.ezeiza

import java.io.File
import javax.imageio.ImageIO
import java.io.StringWriter
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import java.text.SimpleDateFormat
import java.util.Date

object Ezeiza extends App {

  //val logger = Logger.getLogger(getClass)
  
  if(args.length < 2) {
    printUsage
  }
  else {
	  try {
	    
		  val command = args(0)
		  val confPath = args(1)
		  
		  val startDate = if(args.length > 2) {
		    new SimpleDateFormat("HH:mm:ss").parse(args(2))
		  }
		  else {
		    new Date
		  }		  
		  
		  val (dashboards, ordering, photogallery) = init(confPath)
		  
//		  println("Claves")
//		  dashboards.head.cellMap.keys.toList.sorted.foreach(println(_))
		  
		  command match {
		    
		    case "demo" => demo(dashboards)
		    
		    case "server" => server(dashboards, startDate, ordering)
		    
		    case _ => printUsage
		    
		  }

		  
	  }
	  catch {
	    case e: Error => {
	      println(e.getMessage())
	    }
	  }
  }
  
  def init(confPath: String) = {
    
    
		  val confFile = new File(confPath)
		  if(!confFile.exists() || !confFile.isDirectory()) {
		    println("%s no es una carpeta".format(confFile))
		  }
    
		  val cacheDir = new File("/tmp", "imgcache")
		  cacheDir.mkdirs()
		  val photogallery = new Photogallery(confFile, cacheDir)
		  
		  val dashboardFile = confFile.listFiles().find(_.getName().endsWith(".xls"))
		  val (ordering, parsedDashboards) = dashboardFile.map(xlsPath => new XLSReader().parse(xlsPath)).getOrElse({
		    throw new Error("No encontrŽ un archivo de dashboards (xls) en %s".format(confPath))
		  })
		  val dashboards = parsedDashboards.map(d => d.copy(photogallery = photogallery))

		  dashboards.foreach(dashboard => {
		    println("Dashboard %d x %d".format(dashboard.cantFilas, dashboard.cantColumnas))
		    for(f <- 0 to (dashboard.cantFilas - 1)) {
		      for(c <- 0 to (dashboard.cantColumnas - 1)) {
		        print("(%d, %d, \"%s\") ".format(f, c, dashboard.cellAt(f, c).value))
		      }
		      println
		    }
		    println
		    
		  })    
		  
		  (dashboards, ordering, photogallery)
  }
  
  def printUsage = {
    println("Uso: ezeiza.sh [demo|server] path/a/configuracion [inicio(HH:mm:ss)]")
  }
  
  def server(dashboards: List[Dashboard], startDate: Date, ordering: List[String]) = {
	  
	val configPath: String = {
	    val configSetting = System.getProperty("config")
	    if(configSetting != null) configSetting else "application.conf"
	}    
	val config = parseConfig("./" + configPath)
    
    val actorSystem = ActorSystem("BAMSystem", config)

    EzeizaServer.run(actorSystem, config, dashboards, startDate, ordering)
    
  }
  
  def demo(dashboards: List[Dashboard]) = {
    
    val folder = new File("/tmp")
    
    println("Escribiendo demo %s".format(folder.getAbsolutePath()))
    
    dashboards.foreach(d => {
      d.writeDemo(new File(folder, "%d.png".format(d.index)))
    })
    
  }
  
  
  def parseConfig(configFilePath:String): Config = {
    
    val configFile = new File(configFilePath).getAbsoluteFile()
    
    if (!configFile.exists()) {
    	throw new Exception("Configuration file %s doesn't exist.\n Verify route".format(configFilePath))
    }
    
    //logger.info("Reading config file from %s".format(configFile.getAbsolutePath))

    val conf = ConfigFactory.parseFile(configFile)
    
    //logger.info("Configuration loaded from %s\n%s".format(configFilePath, conf.root().render()))
    
    conf
  }    
  
  
}
