package latis.reader.tsml

import scala.annotation.migration
import scala.io.Source

import latis.dm.Dataset
import latis.dm.Function
import latis.dm.Sample
import latis.dm.Scalar
import latis.dm.Tuple
import latis.dm.Variable
import latis.metadata.Metadata
import latis.ops.Operation
import latis.reader.tsml.ml.Tsml
import play.api.libs.json.JsArray
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsResultException
import play.api.libs.json.JsString
import play.api.libs.json.JsValue
import play.api.libs.json.Json

class JsonAdapter(tsml: Tsml) extends TsmlAdapter(tsml) {

  //---- Manage data source ---------------------------------------------------
  
  private var source: Source = null
  
  /**
   * Get the Source from which we will read data.
   */
  def getDataSource: Source = {
    if (source == null) source = Source.fromURL(getUrl)
    source
  }
  
  override def close {
    if (source != null) source.close
  }
  
  
  override def getDataset(ops: Seq[Operation]): Dataset = {
  
    //read entire source into string, join with new line
    val jsonString = getDataSource.getLines.mkString(sys.props("line.separator"))
    val value = Json.parse(jsonString).as[JsObject].value.toSeq
    val name = value(0)._1
    val vars = value(0)._2.as[JsObject].fields.map(a => makeValue(a._2, a._1))
        
    val v = vars.length match {
      case 0 => ???
      case 1 => vars.head
      case _ => Tuple(vars) //wrap multiplevars in a Tuple
    }
    
    Dataset(v, Metadata(name))
  }
  
  def parseValueMap(map: scala.collection.Map[String, JsValue]): Seq[Variable] = {
    map.keys.toSeq.map(k => makeValue(map(k), k))
  }
  
  def makeValue(value: JsValue, name: String): Variable = {
    try {
      val array = value.as[JsArray]
      makeArray(array, name)
    } catch {
      case jre: JsResultException => try {
        val obj = value.as[JsObject]
        makeObject(obj, name)
      } catch {
        case jre: JsResultException => makeScalar(value, name)
      }
    }
  }
  
  def makeScalar(s: JsValue, name: String): Scalar = {
    try {
      val num = s.as[JsNumber].value
      if(num.isValidLong) Scalar(Metadata(name), num.longValue) else Scalar(Metadata(name), num.doubleValue)
    } catch {
      case jre: JsResultException => try {
        Scalar(Metadata(name), s.as[JsString].value)
      } catch {
        case jre: JsResultException => ??? //I think all scalars will be either numbers or strings
      }
    }
  }
  
  def makeArray(arr: JsArray, name: String): Function = {
    val it = arr.value
    Function(it.map(makeSample(_)), Metadata(name))
  }
  
  def makeSample(value: JsValue): Sample = {
    try {
      val obj = value.as[JsObject]
      val vars = obj.fields.map(a => makeValue(a._2, a._1))
      Sample(vars.head, Tuple(vars.tail))
    } catch { case jre: JsResultException => ??? }//this would indicate an index
  }
  
  def makeObject(obj: JsObject, name: String): Tuple = {
    val vars = obj.fields.map(a => makeValue(a._2, a._1))
        
    Tuple(vars, Metadata(name))
  }
}