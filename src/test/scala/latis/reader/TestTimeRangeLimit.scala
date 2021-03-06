package latis.reader

import org.junit.Assert.assertEquals
import org.junit.Test
import latis.ops.Operation
import latis.ops.filter.FirstFilter
import latis.ops.filter.LastFilter
import latis.ops.filter.Selection
import latis.ops.filter.TakeOperation
import latis.ops.filter.TakeRightOperation

class TestTimeRangeLimit {
  
  @Test
  def time_range_equals_limit = {
    val ops = scala.collection.mutable.ArrayBuffer[Operation]()
    ops += Selection("time >= 1970-01-01")
    ops += Selection("time <  1970-01-03")
    val ds = DatasetAccessor.fromName("ascii_with_limit").getDataset(ops)
    assertEquals(2, ds.getLength)
  }
  
  @Test(expected = classOf[UnsupportedOperationException])
  def time_range_exceeds_limit = {
    val ops = scala.collection.mutable.ArrayBuffer[Operation]()
    ops += Selection("time >= 1970-01-01")
    ops += Selection("time <  1970-01-03T00:00:01")
    val ds = DatasetAccessor.fromName("ascii_with_limit").getDataset(ops)
    assertEquals(2, ds.getLength)
  }
  
  @Test(expected = classOf[UnsupportedOperationException])
  def default_max_exceeds_limit = {
    val ops = scala.collection.mutable.ArrayBuffer[Operation]()
    ops += Selection("time > 1970-01-02")
    //Data ends on 1970/01/03 but validation assumes the present date
    val ds = DatasetAccessor.fromName("ascii_with_limit").getDataset(ops)
    assertEquals(1, ds.getLength)
  }
  
  @Test
  def valid_because_of_first_filter = {
    val ops = scala.collection.mutable.ArrayBuffer[Operation]()
    ops += Selection("time > 1970-01-02")
    ops += FirstFilter()
    val ds = DatasetAccessor.fromName("ascii_with_limit").getDataset(ops)
    assertEquals(1, ds.getLength)
  }
  
  @Test
  def valid_because_of_last_filter = {
    val ops = scala.collection.mutable.ArrayBuffer[Operation]()
    ops += Selection("time > 1970-01-02")
    ops += LastFilter()
    val ds = DatasetAccessor.fromName("ascii_with_limit").getDataset(ops)
    assertEquals(1, ds.getLength)
  }
  
  @Test
  def valid_because_of_take_filter = {
    val ops = scala.collection.mutable.ArrayBuffer[Operation]()
    ops += Selection("time > 1970-01-02")
    ops += TakeOperation(1)
    val ds = DatasetAccessor.fromName("ascii_with_limit").getDataset(ops)
    assertEquals(1, ds.getLength)
  }
  
  @Test
  def valid_because_of_takeRight_filter = {
    val ops = scala.collection.mutable.ArrayBuffer[Operation]()
    ops += Selection("time > 1970-01-02")
    ops += TakeRightOperation(1)
    val ds = DatasetAccessor.fromName("ascii_with_limit").getDataset(ops)
    assertEquals(1, ds.getLength)
  }
  
  @Test(expected = classOf[UnsupportedOperationException])
  def take_more_than_1 = {
    val ops = scala.collection.mutable.ArrayBuffer[Operation]()
    ops += Selection("time >= 1970-01-02")
    ops += TakeOperation(2)
    val ds = DatasetAccessor.fromName("ascii_with_limit").getDataset(ops)
    assertEquals(2, ds.getLength)
  }
  
  @Test(expected = classOf[UnsupportedOperationException])
  def takeRight_more_than_1 = {
    val ops = scala.collection.mutable.ArrayBuffer[Operation]()
    ops += Selection("time >= 1970-01-02")
    ops += TakeRightOperation(2)
    val ds = DatasetAccessor.fromName("ascii_with_limit").getDataset(ops)
    assertEquals(2, ds.getLength)
  }
  
  @Test
  def valid_because_of_tsml_max = {
    val ops = scala.collection.mutable.ArrayBuffer[Operation]()
    ops += Selection("time > 1970-01-02")
    //Data ends on 1970/01/03 which is specified in the TSML
    val ds = DatasetAccessor.fromName("ascii_with_limit_and_max").getDataset(ops)
    assertEquals(1, ds.getLength)
  }
}