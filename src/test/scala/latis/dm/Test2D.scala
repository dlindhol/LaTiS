package latis.dm

import latis.dm.implicits._
import org.junit._
import Assert._
import com.typesafe.scalalogging.slf4j.Logging
import latis.dm._
import latis.metadata.Metadata
import latis.time.Time
import latis.writer.AsciiWriter
import latis.writer.Writer
import latis.data.Data
import latis.data.SampledData
import latis.data.seq.DataSeq

class Test2D {
  
  //@Test
  def write_scalar_function_3x3 = AsciiWriter.write(Test2D.scalar_function_3x3)
  
  @Test
  def scalar_function_3x3 {
    val data = Test2D.scalar_function_3x3.toDoubles
    assertEquals(4, data(2)(8), 0.0)
  }
  
//  //@Test
//  def scalar_function_3x3_length {
//    val l = Test2D.scalar_function_3x3.getLength
//    assertEquals(9, l)
//  }
}

/**
 * Static two-dimensional variables to use for testing.
 */
object Test2D {
  
  def scalar_function_3x3 = {
    val x = Integer(Metadata("X"))
    val y = Integer(Metadata("Y"))
    val a = Integer(Metadata("A"))
    
    val domainData = DataSeq(for (x <- 0 until 3; y <- 0 until 3) yield Data(x) concat Data(y))
    val rangeData = DataSeq(Seq.tabulate(3, 3)((x,y) => Data(x+y)).flatten)
    val data = SampledData(domainData, rangeData)
    
    Dataset(Function(Tuple(x,y), a, data = data))
  }
  
  //TODO: support 2D Seq from Seq.tabulate
}