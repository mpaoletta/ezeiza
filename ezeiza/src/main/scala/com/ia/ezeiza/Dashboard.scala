package com.ia.ezeiza

import javax.imageio.ImageIO
import com.google.common.base.Splitter
import java.io.File
import scala.collection.JavaConverters._
import java.awt.image.BufferedImage

case class Dashboard(index: Int, rows: List[List[CellData]], anchoTablero: Long, altoTablero: Long, photogallery: Photogallery, delay: Int) {
  
//  val cellMap = {
//   rows.foldLeft(Map[(Int, Int), CellData]())((m, row) => row.foldLeft(m)((m, c) => m + ((c.row, c.col) -> c)))
//  }
  
  val dotSplitter = Splitter.on('.')
  
  def dimensions = (rows.head.size, rows.size)
  
//  lazy val (razonAncho, razonAlto) = {
//    val imgDimensions = photogallery.standardDimensions
//	(imgDimensions._1.toDouble / anchoTablero, imgDimensions._2.toDouble / altoTablero)
//  }
  
  lazy val imageWidth = photogallery.standardDimensions._1.toInt
  lazy val imageHeight = photogallery.standardDimensions._2.toInt  
  
  def cellAt(y: Int, x: Int) = rows(y)(x)//cellMap((x,y))// rows(y)(x)
  
  def cellContent(y: Int, x: Int): File = {
    
    val cell = cellAt(x, y)
    
    cellContent(cell)
    
  }
  
  private def relativeX(cellx: Long): Double = 
    cellx.toDouble / anchoTablero.toDouble
  
  private def relativeY(celly: Long): Double = 
    celly.toDouble / altoTablero.toDouble
  
  private def cell2imgX(x: Long): Int = 
    (relativeX(x) * imageWidth).toInt
  
  private def cell2imgY(y: Long): Int = 
    (relativeY(y) * imageHeight).toInt
  
  def cellContent(cell: CellData): File = {
    
    val value = cell.value
    
    if(value.endsWith(".jpg"))
    	photogallery.portion(value, cell.col, cell.row, relativeX(cell.x), relativeY(cell.y), relativeX(cell.width), relativeY(cell.height))
    else
      photogallery.character(value)
    
  }
  

  def writeDemo(file: File) = {
    
	def createBlankDashboard = {
	    
		val bi = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
	    val raster = bi.getRaster()
	    for(i <- 0 to imageHeight.toInt - 1)
	      for(j <- 0 to imageWidth.toInt - 1) {
	        raster.setSample(j, i, 0, 0x0)
	        raster.setSample(j, i, 1, 0x0)
	        raster.setSample(j, i, 2, 0x0)
	      }
	
	    bi
  }    

    val demo = createBlankDashboard

	
    def overlayImage(file: File, x: Int, y: Int) = {
    	val g = demo.getGraphics()
    	val portion  = ImageIO.read(file)
    	g.drawImage(portion, x, y, null)      
    } 
    
    for(row <- rows) {
      for(cell <- row) {
    	  val content = cellContent(cell)
	      overlayImage(content, cell2imgX(cell.x), cell2imgY(cell.y))

      }
    }
    
    // Save as new image
    ImageIO.write(demo, "PNG", file);
    
  }
  
  
  def cantFilas = rows.size
  def cantColumnas = rows(0).size
  
  
}

case class CellData(row: Int, col: Int, x: Long, y: Long, width: Int, height: Int, value: String)
