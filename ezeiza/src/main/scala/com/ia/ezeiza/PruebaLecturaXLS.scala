package com.ia.ezeiza

import org.apache.poi.ss.usermodel.Workbook
import java.io.PushbackInputStream
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import java.io.File
import java.io.FileInputStream
import scala.collection.JavaConverters._
import org.apache.poi.ss.usermodel.Cell
import java.text.DecimalFormat

object PruebaLecturaXLS {

  val anchoImagenes = 3264
  val altoImagenes = 1276
  
//  val path = "src/test/sample/prueba.xls"
  val path = "src/test/sample/tablero.xls"
 
  val wb = workbook(new FileInputStream(new File(path)))
  val sheet = wb.getSheetAt(0)
  val decimalFormat = new java.text.DecimalFormat("##############")
  
  var x = 0
  var y = 0

  var cellPositions = Map[(Int, Int), (Int, Int, Int, Int, String)]()
  
  for(row <- sheet.rowIterator().asScala) {
    val rowNum = row.getRowNum()
    val height = row.getHeight()
    val rowCells = for(cell <- row.cellIterator().asScala) {
      val colNum = cell.getColumnIndex()
      val width = sheet.getColumnWidth(colNum)
      val value = readString(cell)
      if(!value.trim.isEmpty())
    	  cellPositions = cellPositions + ((colNum, rowNum) -> (x, y, width, height, value))
      println("(%d, %d) = (%d, %d, %d, %d, %s)".format(colNum, rowNum, x, y, width, height, value))
      
      val newCell = CellData(rowNum, colNum, x, y, width, height, value)
      
      x = x + width
      y = y + height
    }
  }
  
  val anchoTablero = x
  val altoTablero = y
  val df = new DecimalFormat("#.####")

  val razonAncho = anchoImagenes.toDouble / anchoTablero
  val razonAlto = altoImagenes.toDouble / altoTablero
//  
  
  def normAncho(x: Int) = Math.round(x.toDouble * razonAncho)
  def normAlto(y: Int) = Math.round(y.toDouble * razonAlto)
  
  val normalizedCells = cellPositions.map(kv => {
    
    val (coords, cell) = kv
    
    val (cellx, celly, width, height, value) = cell
    
//    anchoTotal -> anchoImagen
//    posx -> x 
//    x = anchoTotal / (posx*anchoImagen)
    
    val normalizedCell = (normAncho(cellx), normAlto(celly), 
        normAncho(width), normAlto(height), value)
    
    (coords, normalizedCell)
  })
  
  println("Celdas normalizadas")
  
  for(kv <- normalizedCells) {
    
    println("%s -> %s".format(kv._1, kv._2))
    
  }
  
  println("Tama–o tablero: (%d x %d)".format(anchoTablero, altoTablero))
  
  println("Razon ancho: " + df.format(razonAncho))
  println("Razon alto: " + df.format(razonAlto))  
  
  
  
  def readString(cell: Cell): String = {

    var stringVal: String = null
    if(cell!=null) {
      stringVal = cell.getCellType match {
        case Cell.CELL_TYPE_NUMERIC => decimalFormat.format(cell.getNumericCellValue)
        case Cell.CELL_TYPE_STRING => cell.getStringCellValue.trim
        case Cell.CELL_TYPE_BLANK => " "
        case Cell.CELL_TYPE_BOOLEAN => String.valueOf(cell.getBooleanCellValue)
        case Cell.CELL_TYPE_FORMULA => "Formula"
        case Cell.CELL_TYPE_ERROR => "Error: " + cell.getErrorCellValue
      }

    }
    stringVal
  }
  
  
  def workbook(is: java.io.FileInputStream): Workbook = {
    create(is)
  }     
    
  protected def create(inp: java.io.InputStream): Workbook = {
    var inpa = inp
    if(! inpa.markSupported()) {
    	inpa = new PushbackInputStream(inp, 8);
    }
    if(POIFSFileSystem.hasPOIFSHeader(inpa)) {
    	new HSSFWorkbook(inpa);
    }
    else
      throw new RuntimeException("Error, tipo de archivo no soportado")
    
  }  
  
}

