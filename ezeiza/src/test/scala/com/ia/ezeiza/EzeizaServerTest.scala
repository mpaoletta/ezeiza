package com.ia.ezeiza

import java.util.Date


/**
 * TODO migrate to scalatest or something
 */
object EzeizaServerTest extends App {
  
  val (dashboards, ordering, photogallery) = Ezeiza.init("/Users/martinpaoletta/repositorio/ia/ezeiza/ezeiza/src/test/sample/demo")
  
  val bc = conf(1, 1)
  
  println(bc)

  
  private def conf(x: Int, y: Int) = {
	val phases = dashboards.map(dashboard => CellPhase(dashboard.cellContent(y, x).getName, dashboard.delay))
	val cellOrdering = ordering.map(photogallery.portionName(_, x, y))
	
	val newId = Some("test")
	
	val conf = BoardConfiguration(newId.get, new Date().getTime, phases, cellOrdering, newId.map(_ => true).getOrElse(false))
    	    	
	conf
  }
  
  
}