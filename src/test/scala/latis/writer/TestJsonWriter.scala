package latis.writer

import org.junit._
import Assert._
import latis.dm._
import latis.metadata.Metadata
import latis.data.SampledData
import java.io.ByteArrayOutputStream

class TestJsonWriter extends WriterTest {

  @Test
  def test_dap2 {
    test_writer(getDataset("dap2"),"json")
  }
  @Test
  def test_fof {
    test_writer(getDataset(fof),"json")
  }
  @Test
  def test_scalar {
    test_writer(getDataset("scalar"),"json")
  }
  @Test
  def test_tsi {
    test_writer(getDataset("tsi"),"json")
  }
  @Test
  def test_tof {
    test_writer(getDataset(tof),"json")
  }
  
  //@Test
  def print_json {
    print(fof, "json")
  }
  
  //@Test 
  def write_json_file {
    //for(name <- names)
    write_to_file(fof, "json")
  }
  
  //@Test
  def testj = Writer.fromSuffix("json").write(TestDataset.index_function)
  
  //@Test 
  def empty_dataset = Writer.fromSuffix("json").write(TestDataset.empty)
  
  //@Test
  def empty_function = Writer.fromSuffix("json").write(TestDataset.empty_function)
  
  //TODO: need delimiters for top level vars
  @Test @Ignore
  def multple_top_level_variables {
    val r = Real(Metadata("myReal"), 3.14)
    val t = Text(Metadata("myText"), "Hi")
    val i = Integer(Metadata("myInt"), 3)
    val ds = Dataset(List(r,t,i), Metadata("three_scalars"))
    
    //write to string then split on "," to make sure we got 3 components
    val out = new ByteArrayOutputStream()
    Writer(out, "json").write(ds)
    val s = out.toString()
    val ss = s.split(",")
    assertEquals(3, ss.length) 
  }
  
  //@Test
  def missing_value {
    val domain = Real(Metadata(Map("name" -> "domain")))
    val range = Real(Metadata(Map("name" -> "range", "missing_value" -> "0")))
    val data = SampledData.fromValues(List(0,1,2,3), List(1,2,0,4))
    val ds = Dataset(Function(domain, range, data = data))
    
    //Writer.fromSuffix("csv").write(ds)
    Writer.fromSuffix("jsond").write(ds)
  }
  
  //@Test
  def nested_function {
    val ds = Dataset(TestNestedFunction.function_of_functions_with_sampled_data)
    Writer.fromSuffix("json").write(ds)
    
  }  
  
  //@Test
  def test {
    val ds = getDataset(fof) //TestDataset.canonical
    Writer.fromSuffix("json").write(ds)
  }
}