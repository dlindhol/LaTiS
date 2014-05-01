package latis.writer

import latis.dm.Dataset
import latis.dm.Index
import latis.dm.Sample
import latis.dm.Scalar
import latis.dm.Tuple
import latis.dm.Variable
import latis.time.Time

/**
 * Return data as nested arrays, without metadata.
 * One inner array for each sample.
 * Time will be presented in native JavaScript time: milliseconds since 1970.
 * Handy for clients that just want the data (e.g. HighCharts).
 */
class CompactJsonWriter extends JsonWriter {

  override def makeHeader(dataset: Dataset) = ""
  override def makeFooter(dataset: Dataset) = ""
  
  override def makeLabel(variable: Variable): String = ""
    
  /**
   * Override to present time in native JavaScript units: milliseconds since 1970.
   */
  override def makeScalar(scalar: Scalar): String = scalar match {
    case t: Time => t.getJavaTime.toString  //use java time for "compact" json
    case _ => super.makeScalar(scalar)
  }
  
  override def makeSample(sample: Sample): String = {
    val Sample(d, r) = sample
    d match {
      case _: Index => varToString(r) //drop Index domain
      case _ => varToString(Tuple(d.toSeq ++ r.toSeq)) //combine domain and range vars into one Tuple
      //TODO: not tested for nested variables
    }
  }
    
  /**
   * Represent a tuple as an array with each element being an array of values.
   */
  override def makeTuple(tuple: Tuple): String = {
    tuple.getVariables.map(varToString(_)).mkString("[", ",", "]") 
  }
}