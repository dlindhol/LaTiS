package latis.dm

import latis.data.value.IndexValue
import latis.metadata.Metadata
import latis.data.Data

/**
 * Scalar Variable that represents an index.
 * Implemented such that its Data represents the index values.
 * This is intended as a placeholder for domain values when no domain variable is available.
 * It should not be used to model integers in general.
 */
trait Index extends Scalar with Number


object Index {
  
  def apply(): Index = new AbstractScalar(metadata = Metadata("index")) with Index

  def apply(value: Int): Index = new AbstractScalar(metadata = Metadata("index"), data = IndexValue(value)) with Index
  
  def apply(data: Data): Index = new AbstractScalar(metadata = Metadata("index"), data = data) with Index
  
  def apply(md: Metadata, v: Int): Index = new AbstractScalar(metadata = md, IndexValue(v)) with Index

  def apply(md: Metadata, data: Data): Index = new AbstractScalar(metadata = md, data = data) with Index

  /**
   * Expose the index value represented by this Variable.
   */
  def unapply(index: Index): Option[Int] = Some(index.getNumberData.intValue)
}