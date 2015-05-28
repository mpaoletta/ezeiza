package com.ia.ezeiza

import javax.imageio.ImageIO
import java.io.StringWriter
import java.io.File
import java.awt.image.BufferedImage


class Photogallery(path: File, cachePath: File) {

	val ini = System.currentTimeMillis()
	println("Leyendo galeria de fotos en %s".format(path.getAbsolutePath()))
	
	def listImages(file: File) = file.listFiles().filter(_.getName().toLowerCase().endsWith(".jpg"))
	
    val images = listImages(path)
    var cache = Map[String, BufferedImage]()
  
    validar()
	
    val alpha = listImages(new File(path, "alpha"))

	val standardDimensions = if(images.isEmpty) (0d, 0d) else imageDimensions(images.head)

	def imagesAsMap(imgs: Iterable[File]) = imgs.foldLeft(Map[String, File]())((m, f) => m + (f.getName() -> f))
	
	val imgMap = imagesAsMap(images) ++ imagesAsMap(alpha)
	
	println("Galeria leida en %d ms".format(System.currentTimeMillis() - ini))
	
	def validar() = {
		if(!images.isEmpty) {
			// Validar tama–os
			println("Validando tama–os")
			val dims = images.map(f => (f, imageDimensions(f)))
		    val errors = dims.filter(fd => fd._2 != dims.head._2)
		    if(!errors.isEmpty) {
			    val sw = new StringWriter
			    sw.append("Error, las siguientes imagenes no respetan las dimensiones %s:\n".format(dims.head._2))
			    errors.foreach(fd => sw.append("- %s\n".format(fd._1.getName)))
			    throw new Error(sw.toString())
		    }
	}	  
	}
	
	def imageDimensions(file: File): (Double, Double) = {
	  print(".")
	  val img = ImageIO.read(file)
	  cache = cache + (file.getName -> img)
	  (img.getWidth(), img.getHeight())	  
	}
	
	
	
	
	def portion(imgName: String, col: Int, row: Int, relativeX: Double, relativeY: Double, relativeWidth: Double, relativeHeight: Double) = {
		if(col < 0 || row < 0)
		  println("debug")
	  
		val portionFile = new File(cachePath, portionName(imgName, col, row))
		if(portionFile.exists)
		  portionFile
		else {
		  val x = (relativeX * standardDimensions._1).toInt
		  val y = (relativeY * standardDimensions._2).toInt
		  val width = (relativeWidth * standardDimensions._1).toInt
		  val height = (relativeHeight * standardDimensions._2).toInt
		  
		  val bufferedImage = cache(imgName)
		  
		  val portion = bufferedImage.getSubimage(x, y, width, height)
		  ImageIO.write(portion, "jpg", portionFile)
		  
		  portionFile
		}
	}
	
	def character(name: String): File = {
	  new File(new File(path, "alpha"), name + ".jpg")
	}

	def imageForName(imgName: String) = getFromCache(imgName).getOrElse(character(imgName))
	
	def getFromCache(imgName: String): Option[File] = {
	  val cacheFile = new File(cachePath, imgName)
	  if(cacheFile.exists) Some(cacheFile) else None
	}
	
	def portionName(imageName: String, col: Int, row: Int) = {
	  val imgName = if(imageName.endsWith(".jpg")) imageName else imageName + ".jpg"
	  println("Buscando " + imgName)
	  println("Alfa: ")
	  alpha.foreach(println(_))
	  val resultado = if(alpha.find(_.getName() == imgName).isDefined) {
		  imgName 
	  } 
	  else {
		  "%s_%dx%d.jpg".format(imgName, col, row)	
	  }
	  println(resultado)
	  resultado
	}
}
