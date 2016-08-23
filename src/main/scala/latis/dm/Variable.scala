package latis.dm

import latis.data.Data
import latis.data.value.DoubleValue
import latis.data.NumberData
import latis.metadata.Metadata
import scala.collection.Seq
import latis.time.Time
import latis.data.value.StringValue
import latis.data.value.LongValue
import latis.time.TimeFormat

/**
 * Base type for all Variables in the LaTiS data model.
 */
trait Variable {
  def getMetadata(): Metadata //need () to avoid ambiguity
  def getMetadata(name: String): Option[String] = getMetadata.get(name)
  def getData: Data
  
  def getName: String
  
  def isNumeric: Boolean = getData.isInstanceOf[NumberData]
  def getNumberData: NumberData = getData.asInstanceOf[NumberData]
  //TODO: deal with TextData
  //TODO: empty data, Option?
  
//  def getLength: Int
  def getSize: Int
  
  def findVariableByName(name: String): Option[Variable]
  def findAllVariablesByName(name: String): Seq[Variable]
  def findFunction: Option[Function]
  def hasName(name: String): Boolean
  
  def toSeq: Seq[Scalar]
  
  //Experimental: to help build a Scalar from an existing Scalar but with new metadata.
  //should also match tsml element names
  def getType: String = this match {
    case _: Dataset  => "dataset"
    case _: Tuple    => "tuple"
    case _: Function => "function"
    //case _: Time     => "time"  //TODO: how to deal with real vs int...?
    case _: Index    => "index"
    case _: Real     => "real"
    case _: Integer  => "integer"
    case _: Text     => "text"
    case _: Binary   => "binary"
  }
  
  //experimental: build from template with data
  //TODO: consider updatedData akin to updatedMetadata (and scala Map.updated)
  //TODO: consider scala's CanBuildFrom...
  //TODO: pattern match here or do in subclasses?
  //TODO: don't confuse with function evaluation, call this "copy"...?
  def apply(data: Data): Variable = this match {
    //TODO: make sure Data is valid, length
    case t: Time => {
      val scale = t.getUnits
      val md = t.getMetadata
      t match {
        case _: Text => data match {
          case sv: StringValue => new Time(scale, md, sv) with Text
          //If numeric, assume ms since 1970 and convert to original time format
          case LongValue(l) => {
            md.get("units") match {
              case Some(s) => {
                val time = TimeFormat(s).format(l)
                new Time(scale, md, StringValue(time)) with Text
              }
              case None => throw new UnsupportedOperationException("Time has no units.")
            }
          }
          case DoubleValue(d) => {
            md.get("units") match {
              case Some(s) => {
                val time = TimeFormat(s).format(d.toLong)
                new Time(scale, md, StringValue(time)) with Text
              }
              case None => throw new UnsupportedOperationException("Time has no units.")
            }
          }
          case _ => throw new UnsupportedOperationException("Text must be constructed with a StringValue.")
        }
        case _: Real => data match {
          case dv: DoubleValue => new Time(scale, md, data) with Real
          case _ => throw new UnsupportedOperationException("Real must be constructed with a DoubleValue.")
        }
        case _: Integer => data match {
          case lv: LongValue => new Time(scale, md, data) with Integer
          case dv: DoubleValue => new Time(scale, md, data) with Integer
          case _ => throw new UnsupportedOperationException("Integer must be constructed with a LongValue.")
        }
      }
    }
    case _: Real    => data match {
      case dv: DoubleValue => Real(this.getMetadata, dv)
      case _ => throw new UnsupportedOperationException("Real must be constructed with a DoubleValue.")
    }
    case _: Integer => data match {
      case lv: LongValue => Integer(this.getMetadata, lv)
      case dv: DoubleValue => Integer(this.getMetadata, LongValue(dv.longValue))
      case _ => throw new UnsupportedOperationException("Integer must be constructed with a LongValue.")
    }
    case _: Text    => data match {
      case sv: StringValue => Text(this.getMetadata, sv)
      case dv: DoubleValue => Text(this.getMetadata, StringValue(dv.doubleValue.toString))
      case _ => throw new UnsupportedOperationException("Text must be constructed with a StringValue.")
    }
  }
}

