package latis.data

import latis.dm._
import latis.ops.math._
import java.nio.ByteBuffer
import latis.data.value._
import latis.data.buffer.ByteBufferData
import java.nio.Buffer

/*
 * TODO: 2013-05-30
 * simplifying assumptions:
 *   ok for scalars to have array Data
 *     like column oriented database
 *     implicit IndexFunction
 *     FunctionIterator can stitch them together
 *     Arrays or ByteBuffers?
 *   outer function can have Data (Iterable) to support sample iteration
 *     ByteBuffer
 *     what about Text? IndexFunction of Chars?
 *   don't worry about iterating into nested functions, yet
 *   embrace sample/record abstraction (outer function)
 * 
 * 2013-08-05
 * ultimate form of the data is array of bytes
 * any other form should be a convenience wrapper/producer
 * getDouble... should not be available for Data in general
 *   use via pattern matching
 *   use NumberData trait?
 * but pattern matching will make instance of value class
 *   matching on the Variable (e.g. Real) instead
 */

/*
 * TODO: WrappedData?
 * stride for subset, used during access, on top of orig data
 */

trait Data extends Any {
  //TODO: head::tail semantics? Stream?
  //TODO: word = Array of 4 chars, 8 bytes
  //TODO: String as Index array of Char, or Word?
  //TODO: Blob: fixed length byte array
  //TODO: def apply(index: Int): Any = value if 0 else IOOB?
  
  def length: Int  //number of records, Experimental: "-n" is unlimited, currently n
  def recordSize: Int //bytes per record
  def size = length * recordSize //total number of bytes
  
  def getByteBuffer: ByteBuffer 
  //TODO: just byteBuffer?
  
  //TODO: beware of mixing getters that increment with iterator
  def iterator: Iterator[Data] //= List(DoubleValue(doubleValue)).iterator
  //TODO: support foreach, (d <- data)
  
//  def getDouble: Option[Double]
//  def getString: Option[String]
//  
//  def doubleValue: Double = getDouble match {
//    case Some(d) => d
//    case None => throw new Error("No Data") //null
//  }
//  
//  def stringValue: String = getString match {
//    case Some(s) => s
//    case _ => throw new Error("No Data")
//  }
    
  def isEmpty: Boolean = length == 0
  def notEmpty = ! isEmpty
  
  
  //TODO: is byte buffer equality sufficient?
  override def equals(that: Any) = that match {
    case d: Data => d.getByteBuffer == getByteBuffer
    case _ => false
  }
  
  override def hashCode = getByteBuffer.hashCode
}

//-----------------------------------------------------------------------------

object Data {
  
  val empty = EmptyData
  
  def apply(d: Double): Data = DoubleValue(d)
  def apply(s: String): Data = StringValue(s)
  def apply(ds: Seq[Double]): Data = SeqData(ds)
  //TODO: varargs?
  
  //takes Buffer so user's don't have to cast after "flip"
//TODO: take number of samples instead of record size?
  def apply(buffer: Buffer, sampleSize: Int): Data = {
    //flip if not positioned at 0
    val b = if (buffer.limit > 0 && buffer.position != 0) buffer.flip() else buffer
    b match {
      case bb: ByteBuffer => new ByteBufferData(bb, sampleSize)
//TODO:      case cb: CharBuffer => 
      /*
       * TODO: do we ever want any other subclass of Buffer?
       * YES, CharBuffer, and DoubleBuffer might be handy
       */
    }
  }
  
  //to be used for a single sample: sampleSize = limit
  def apply(buffer: Buffer): Data = Data(buffer, buffer.limit)
  
  //Concatenate Data
  def apply(data: Seq[Data])(implicit ignore: Data): Data = { //implicit hack for type erasure ambiguity
    val size = data.foldLeft(0)(_ + _.size) //total size of all elements
    val buffer = data.foldLeft(ByteBuffer.allocate(size))(_ put _.getByteBuffer) //build ByteBuffer with all Data
    val bb = buffer.flip.asInstanceOf[ByteBuffer] //reset to the beginning and set the "limit" to the actual size
    Data(bb)
  }
  
//  def apply(dit: Iterator[Data]) : Data = new Data {
//    override def iterator = dit
//    //TODO: deal with recordSize, length..., unknown
//  }
}
