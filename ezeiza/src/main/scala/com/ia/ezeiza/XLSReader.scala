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
import org.apache.poi.ss.usermodel.Sheet
import com.google.common.base.Splitter


class XLSReader {

  val decimalFormat = new java.text.DecimalFormat("##############")     

  def parse(path: String): (List[String], List[Dashboard]) = {
	  parse(new File(path))
  }
  
  def parse(path: File): (List[String], List[Dashboard]) = {
    
	val wb = workbook(new FileInputStream(path))
	println("Leyendo orden de tambores")
	val ordering = readOrdering(wb.getSheetAt(0))
	println("Leyendo delays entre fases")
	val delays = readDelays(wb.getSheetAt(0))
	val dashboards = for(i <- 1 to wb.getNumberOfSheets()-1) 
	  yield parseDashboard(i, wb.getSheetAt(i), delays(i-1))
  
	println("Tiempo de obra: %d segundos".format(dashboards.map(_.delay).sum / 1000))
	
	(ordering, dashboards.toList)
  }
  
  def readOrdering(sheet: Sheet): List[String] = 
    (for(row <- sheet.rowIterator().asScala) yield readString(row.getCell(0))).toList.tail
  
  def readDelays(sheet: Sheet): List[Int] = 
    (for(row <- sheet.rowIterator().asScala) yield readString(row.getCell(1))).filter(_ != null).toList.tail.map(_.toInt)
    
  def parseDashboard(index: Int, sheet: Sheet, delay: Int): Dashboard = {
    
	  var x = 0
	  var y = 0
	  var rows = List[List[CellData]]()
	  println("Dashboard %d".format(index))
	  for(row <- sheet.rowIterator().asScala) {
	    val rowNum = row.getRowNum()
	    val height = row.getHeight()
	    var cols = List[CellData]()
	    x = 0
	    for(cell <- row.cellIterator().asScala) {
	      val colNum = cell.getColumnIndex()
	      val width = sheet.getColumnWidth(colNum)
	      val value = readString(cell)
	      if(!value.trim.isEmpty()) {
	    	  println("(%d,%d) = %s".format(rowNum, colNum, value))
	    	  cols = CellData(rowNum, colNum, x, y, width, height, value) :: cols
	      }
	      x = x + width
	    }
	    y = y + height
	    
	    rows = cols.reverse :: rows
	  } 
	  
	  rows = rows reverse
	  
	  println("Dashboard comprimido:")
	  
	  var r = 0
      var cols = Set[Int]()
	  // Comprimir filas
      val movedRows = rows.filter(!_.isEmpty).map(currentRow => {
		  cols = cols ++ currentRow.map(_.col)
		  val movedRow = if(currentRow(0).row > r) currentRow.map(_.copy(row = r)) else currentRow
		  r = r + 1
		  movedRow
	  })
      // Comprimir columnas
	  val (minCol, maxCol) = (cols.min, cols.max)
      val allCols = 0 to maxCol-1
	  val missingCols = (cols.max+1 :: (allCols.toSet -- cols).toList).sorted
      println("Missing cols: " + missingCols)
	  val compressedRows = movedRows.map(current => {
		  val compressed = current.map(cell => {
		    val numOfMissing = missingCols.find(_ > cell.col).map(missingCol => missingCols.indexOf(missingCol) + 1).getOrElse(0)
		    cell.copy(col = cell.col - numOfMissing)
		  })
		  compressed
	  })
	  
	  compressedRows.foreach(_.foreach(cell => println("(%d,%d) = %s".format(cell.row, cell.col, cell.value))))
	  
	  Dashboard(index, compressedRows, x, y, null, delay)
  }
  
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
  
  
  private def workbook(is: java.io.FileInputStream): Workbook = {
    create(is)
  }     
    
  private def create(inp: java.io.InputStream): Workbook = {
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


