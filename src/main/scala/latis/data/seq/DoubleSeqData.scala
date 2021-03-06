package latis.data.seq

import latis.data.IterableData
import latis.data.value.DoubleValue

import java.nio.ByteBuffer

import scala.collection.immutable

class DoubleSeqData(ds: immutable.Seq[Double]) extends IterableData {
  
  override def getByteBuffer: ByteBuffer = ds.foldLeft(ByteBuffer.allocate(size))(_.putDouble(_)).rewind.asInstanceOf[ByteBuffer]
  
  override def length: Int = ds.length
  def recordSize: Int = 8
  
  def iterator: Iterator[DoubleValue] = ds.iterator.map(DoubleValue(_))
  
  def apply(index: Int): DoubleValue = DoubleValue(ds(index))
}
