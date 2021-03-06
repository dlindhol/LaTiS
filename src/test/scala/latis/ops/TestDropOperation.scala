package latis.ops

import scala.collection.mutable.ArrayBuffer

import latis.dm.Function
import latis.dm.implicits._
import latis.dm.Real
import latis.metadata.Metadata
import latis.data.SampledData
import org.junit._
import Assert._
import latis.ops.filter._
import latis.dm.Dataset
import latis.dm.TestDataset
import latis.dm.Sample
import latis.writer.Writer
import latis.dm.Integer
import latis.dm.Tuple
import latis.dm.Text
import latis.dm.Index
import latis.time.Time
import latis.reader.DatasetAccessor
import latis.reader.tsml.TsmlReader
import latis.writer.AsciiWriter


class TestDropOperation {
  
  @Test
  def drop_0 {
    val ds = DropOperation(0)(TestDataset.canonical)
    ds match {
      case Dataset(Function(s)) => assertEquals(3,s.toList.length)
    }
  }
  
  @Test
  def drop_1 {
    val ds = DropOperation(1)(TestDataset.canonical)
    ds match {
      case Dataset(Function(s)) => s.toList.head match {
        case Sample(_,Tuple(x)) => x match {
          case Seq(Integer(i),Real(r),Text(t)) => {
            assertEquals(2,i)
            assertEquals(2.2,r,0)
            assertEquals("B",t)
          }
        }
      }
    }
  }
  
  @Test
  def drop_2 {
    val ds = DropOperation(2)(TestDataset.canonical)
    ds match {
      case Dataset(Function(s)) => s.toList.head match {
        case Sample(_,Tuple(x)) => x match {
          case Seq(Integer(i),Real(r),Text(t)) => {
            assertEquals(3,i)
            assertEquals(3.3,r,0)
            assertEquals("C",t)
          }
        }
      }
    }
  }
  
  @Test
  def drop_3 {
    val ds = DropOperation(3)(TestDataset.canonical)
    ds match {
      case Dataset(Function(s)) => assertEquals(0,s.toList.length)
      case _ => fail
    }
  }
  
  @Test
  def dropping_empty_dataset {
    val ds = DropOperation(5)(Dataset.empty)
    assertEquals(Dataset.empty, ds)
  }
  
  @Test
  def should_not_drop_scalar {
    val ds = DropOperation(1)(TestDataset.integer)
    ds match {
      case Dataset(x) => x match {
        case Integer(i) => assertEquals(42,i)
      }
    }
  }
  
  @Test
  def should_not_drop_tuple_of_scalars {
    val ds = DropOperation(5)(TestDataset.tuple_of_scalars)
    ds match {
      case Dataset(x) => x match {
        case Tuple(y) => y.head match {
          case Integer(i) => assertEquals(0,i)
        }
      }
    }
  }
  
  @Test
  def function_of_scalar_with_drop_0 {
    val ds = DropOperation(0)(TestDataset.function_of_scalar)
    assertEquals(3, ds.getLength)
  }
  
  @Test
  def function_of_scalar_with_drop_1 {
    val ds = DropOperation(1)(TestDataset.function_of_scalar)
    assertEquals(2, ds.getLength)
  }
  
  @Test
  def function_of_scalar_with_drop_3 {
    val ds = DropOperation(3)(TestDataset.function_of_scalar)
    assertEquals(0, ds.getLength)
  }
  
  @Test
  def function_of_scalar_with_drop_5 {
    val ds = DropOperation(5)(TestDataset.function_of_scalar)
    assertEquals(0, ds.getLength)
  }
  
  @Test
  def function_of_function_with_drop {
    val temp = DropOperation(3)(TestDataset.function_of_functions)
    val len = temp.getLength
    assertEquals(1,len)
  }
  
  @Test
  def empty_function {
    val emptyfunc = Dataset(Function(Real(Metadata("domain")), Real(Metadata("range")), Iterator.empty, Metadata(Map("length" -> "0"))), Metadata("empty_function"))
    val ds = DropOperation(5)(TestDataset.empty_function)
    ds match {
      case Dataset(x) => x match{
        case Function(it) => assertEquals(true,it.isEmpty)
      }
    }
  }
  
  @Test
  def drop_using_tsml_data {
    val data = DatasetAccessor.fromName("data_with_marker").getDataset
    val ds = DropOperation(9)(data)
    ds match {
      case Dataset(x) => x match {
        case Function(f) => f.toList.head match {
          case Sample(Real(r1),Real(r2)) => {
            assertEquals(1628.5,r1,0)
            assertEquals(1360.4767,r2,0)
          }
        }
      }
    }
  }
  
  @Test
  def drop_using_tsml_data_with_ops {
    val ops = ArrayBuffer[Operation]()
    ops += Operation("drop",List("2"))
    val ds = DatasetAccessor.fromName("data_with_marker").getDataset(ops)
    ds match {
      case Dataset(x) => x match {
        case Function(f) => f.toList.head match {
          case Sample(Real(r1),Real(r2)) => {
            assertEquals(1621.5,r1,0)
            assertEquals(1360.4856,r2,0)
          }
        }
      }
    }
  }

}