package latis.ops

import org.junit._
import Assert._
import latis.reader.tsml.TsmlReader
import latis.writer._
import latis.ops.agg.CollectionAggregation
import latis.ops.agg.TileAggregation
import latis.dm.Dataset
import latis.dm.Function
import latis.dm.Tuple

class TestAggregation {
  
  //TODO: extend AdapterTests

//  @Test
//  def test_collection_tiles {
//    val ds1 = TsmlReader("datasets/test/agg/scalar_ts_2col_0to9.tsml").getDataset
//    val ds2 = TsmlReader("datasets/test/agg/scalar_ts_2col_10to19.tsml").getDataset
//    
//    val agg = CollectionAggregation()
//    val ds = agg(ds1,ds2)
//    
//    val dss = ds.getVariables
//    assertEquals(2, dss.length)  //still two Functions
//    assertEquals("scalar_ts_2col_0to9: (T -> A)", dss(0).toString)
//    assertEquals("scalar_ts_2col_10to19: (T -> A)", dss(1).toString)
//    //AsciiWriter.write(ds)
//  }
//  
//  @Test
//  def test_collection_diff_types {
//    val ds1 = TsmlReader("datasets/test/agg/scalar_ts_2col_0to9.tsml").getDataset
//    val ds2 = TsmlReader("datasets/test/agg/scalar_ts_3col_0to9.tsml").getDataset
//    
//    val agg = CollectionAggregation()
//    val ds = agg(ds1,ds2)
//    
//    val dss = ds.getVariables
//    assertEquals(2, dss.length)  //still two Functions
//    assertEquals("scalar_ts_2col_0to9: (T -> A)", dss(0).toString)
//    assertEquals("scalar_ts_3col_0to9: (T -> (B, C))", dss(1).toString)
//    //AsciiWriter.write(ds)
//  }
  
  @Test
  def test_append_samples{
    val ds1 = TsmlReader("datasets/test/agg/scalar_ts_2col_0to9.tsml").getDataset
    val ds2 = TsmlReader("datasets/test/agg/scalar_ts_2col_10to19.tsml").getDataset
    
    val agg = TileAggregation()
    val ds = agg(ds1, ds2)
    //AsciiWriter.write(ds)       
    val v = ds match {
      case Dataset(v) => v
      case _ => null
    }
    assert(v.isInstanceOf[Function]) //successfully aggregated into one Function
    assertEquals("(T -> A)", ds.toString)
    assertEquals(20, ds.getLength) //20 samples
  }
      
//  @Test
//  def test_combine_variables {
//    val ds1 = TsmlReader("datasets/test/scalar_ts_2col_0-9.tsml").getDataset
//    val ds2 = TsmlReader("datasets/test/scalar_ts_3col_0-9.tsml").getDataset
//    
//    val agg = ???Aggregation()
//    val ds = agg(ds1,ds2)
//    
//    AsciiWriter().write(ds)
//  }
}
