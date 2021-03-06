package latis.reader.tsml

import latis.data.Data
import latis.reader.tsml.ml.Tsml
import latis.util.LatisProperties

/**
 * Use this Adapter and accompanying TSML to expose LaTiS and system properties.
 */
class PropertiesAdapter(tsml: Tsml) extends TsmlAdapter(tsml) {

  override def init: Unit = {
    getOrigScalarNames.map(vname => {
      val pval = LatisProperties.getOrElse(vname, "")
      appendToCache(vname, Data(pval))
      //deprecate append, TODO: collect DataSeq then add to cache
    }) 
  }
  
  def close: Unit = {}
}
