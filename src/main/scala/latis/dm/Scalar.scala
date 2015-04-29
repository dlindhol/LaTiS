package latis.dm

import latis.data.Data
import latis.data.EmptyData
import latis.data.value.DoubleValue
import latis.data.value.IndexValue
import latis.data.value.LongValue
import latis.data.value.StringValue
import latis.metadata.EmptyMetadata
import latis.metadata.Metadata
import latis.time.Time

/**
 * Base type for all Scalar Variables.
 */
trait Scalar extends Variable {
  
  //move to AbstractScalar since mixing in Time with Text means that anything here overrides Time?
  //note, we tried overriding this in subclasses but ran into inheritance trouble with "new Time with Real"
  def compare(that: String): Int
    
  //TODO: unit conversions...
  def compare(that: Scalar): Int = (this,that) match {
    case (Number(d1), Number(d2)) => d1 compare d2
    case (Text(s1), Text(s2)) => {
      //TODO: is it same to trim each string when comparing or should it happen upstream?
      s1.trim compare s2.trim 
    }
    case _ => throw new Error("Can't compare " + this + " with " + that)
  }

  def getValue: Any
  def getFillValue: Any
  def getMissingValue: Any
  
  def isMissing: Boolean
  
  /**
   * Get a copy of this Scalar with the given metadata property added/replaced.
   */
  def updatedMetadata(kv: (String,String)) = {
    //"updated" like scala.Map, immutable, "update" implies mutable
    //make new Scalar with metadata with new name
    val md = getMetadata + kv //new metadata
    val vtype = getType
    val data = getData
    
    this match {
      case _: Time => Time(vtype, md, data)
      case _ => Scalar(vtype, md, data)
    }
  }
  //TODO: updatedMetadata(md: Metadata)
  
}

object Scalar {
  
  /**
   * Construct a Scalar of the appropriate type based on the type (e.g. as used in TSML).
   */
  def apply(vtype: String, metadata: Metadata = EmptyMetadata, data: Data = EmptyData): Scalar = vtype match {
    case "index"   => new AbstractScalar(metadata, data) with Index
    case "real"    => new AbstractScalar(metadata, data) with Real
    case "integer" => new AbstractScalar(metadata, data) with Integer
    case "text"    => new AbstractScalar(metadata, data) with Text
    case "binary"  => new AbstractScalar(metadata, data) with Binary
    case "time"    => ??? //Time(metadata, data)  
    //TODO: vtype = class name, dynamicly construct 
    case _ => throw new Error("No Scalar type defined for " + vtype)
  }
  
  /**
   * Construct a Scalar of the appropriate type based on the type of the data value.
   */
  def apply(value: AnyVal): Scalar = value match {
    case d: Double => Real(d)
    case f: Float  => Real(f)
    case l: Long   => Integer(l)
    case i: Int    => Integer(i)
    case _ => throw new Error("Unable to make Scalar from value: " + value)
  }
  
  /**
   * Construct a Text Variable with the given string value.
   */
  def apply(value: String) = Text(value)
  
  /**
   * Construct a Scalar of the appropriate type based on the type of the data value, with metadata.
   */
  def apply(metadata: Metadata, value: Any): Scalar = value match {
    //primitive values
    case d: Double => Real(metadata, d)
    case f: Float  => Real(metadata, f)
    case l: Long   => Integer(metadata, l)
    case i: Int    => Integer(metadata, i)
    case s: String => Text(metadata, s)
    //Data Value types
    case dv: DoubleValue => Real(metadata, dv)
    case lv: LongValue   => Integer(metadata, lv)
    case IndexValue(i)   => Index(i)
    case sv: StringValue => Text(metadata, sv)
    
    case _ => throw new Error("Unable to make Scalar from value: " + value)
  }
  
  /**
   * Expose the value represented by this Scalar.
   */
  def unapply(s: Scalar) = Some(s.getValue)
}



