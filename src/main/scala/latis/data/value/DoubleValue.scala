package latis.data.value

import java.nio.ByteBuffer
import latis.data._

//TODO: test if we are getting the benefit of value classes
//  http://docs.scala-lang.org/overviews/core/value-classes.html
//TODO: ValueData trait?

case class DoubleValue(val value: Double) extends AnyVal with NumberData {
  //def length = 1
  def size = 8
  def isEmpty = false
  
  //TODO: put in NumberData? Can we put the impl there and still be a value class?
  //TODO: round or truncate?
  def intValue = value.toInt
  def longValue = value.toLong
  def floatValue = value.toFloat
  def doubleValue = value
  
  def getByteBuffer: ByteBuffer = ByteBuffer.allocate(size).putDouble(doubleValue).rewind.asInstanceOf[ByteBuffer]
  
  //def iterator = List(this).iterator

    //TODO: abstract up for all value classes
//  def apply(index: Int): Data = index match {
//    case 0 => this
//    case _ => throw new IndexOutOfBoundsException()
//  }
  
}
