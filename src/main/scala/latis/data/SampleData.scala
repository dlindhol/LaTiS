package latis.data

import java.nio.ByteBuffer

/**
 * Data for a Function Sample that keeps the domain and range data separate.
 */
case class SampleData(val domainData: Data, val rangeData: Data) extends Data {

  def getByteBuffer: ByteBuffer = ByteBuffer.wrap(domainData.getBytes ++ rangeData.getBytes)
  
  def size: Int = domainData.size + rangeData.size
}

