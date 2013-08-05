package latis.data.value

import java.nio.ByteBuffer
import latis.data.NumberData

//TODO: test if we are getting the benefit of value classes

case class DoubleValue(val value: Double) extends AnyVal with NumberData {
  def length = 1
  def recordSize = 8
  
  def doubleValue = value
  
  def getByteBuffer: ByteBuffer = ByteBuffer.allocate(recordSize).putDouble(doubleValue).rewind.asInstanceOf[ByteBuffer]
  
  def iterator = List(this).iterator
}
