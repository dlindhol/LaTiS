package latis.dm

import scala.collection._
import latis.data._
import latis.metadata._
import java.nio.ByteBuffer
import latis.util.Util

class Function(_domain: Variable, _range: Variable) extends Variable {
  
  /*
   * 2013-08-07
   * TODO: Continuous vs Sampled Function
   * now that Data is separable, how can we support continuous function (e.g. exp model)
   * ContinuousFunction:
   *   apply(domainVal: Variable) => range val
   *   apply(domainSet: Seq[Variable] or Var with SeqData) => SampledFunction
   *   length = -1?
   *   iterator => error
   */

  //expose domain and range vis defs only so we can override (e.g. ProjectedFunction)
  def domain: Variable = _domain
  def range: Variable = _range
  
  private var _iterator: Iterator[Sample] = null
  
  def iterator: Iterator[Sample] = {
    if (_iterator != null) _iterator
    else if (data.isEmpty) iterateFromKids
    else dataIterator.map(Util.dataToSample(_, Sample(domain, range)))
  }
  
  private def iterateFromKids: Iterator[Sample] = {
    val dit = domain.dataIterator.map(data => Util.dataToVariable(data, domain))
    val rit = range.dataIterator.map(data => Util.dataToVariable(data, range))
    (dit zip rit).map(pair => Sample(pair._1, pair._2))
  }

  lazy val length: Int = metadata.get("length") match {
    case Some(l) => l.toInt
    case None =>  { //-1 //unknown
      iterator.length
      //TODO: look at Data, or domain set,..., iterable once problem?
      //TODO: consider wrapped Function
    }
  }
}

object Function {
  //TODO: used named args for data, md?
  
  
  def apply(domain: Variable, range: Variable): Function = {
    new Function(domain, range)
  }  
  
  def apply(domain: Variable, range: Variable, data: Data): Function = {
    val f = new Function(domain, range)
    f._data = data
    f
  }
  
  //build from Iterator[Sample]? TODO: do we need to set _data to something?
  def apply(domain: Variable, range: Variable, sampleIterator: Iterator[Sample]): Function = {
    //Note, wouldn't need domain and range, but would have to trigger iterator, or use peek?
    val f = new Function(domain, range)
    f._iterator = sampleIterator
    f
  }
  
  def apply(domain: Variable, range: Variable, md: Metadata, data: Data): Function = {
    val f = new Function(domain, range)
    f._metadata = md
    f._data = data
    f
  }
  
  def apply(vals: Seq[Seq[Double]]): Function = Function(vals.head, vals.tail: _*)
  
  def apply(dvals: Seq[Double], vals: Seq[Double]*): Function = {
    val domain = Real(dvals)
    //make Real if vals.length == 1
    val range = if (vals.length == 1) Real(vals(0)) else Tuple(vals)(0d) //hack to get around type erasure ambiguity

    new Function(domain, range)
  }
  
  
  def unapply(f: Function): Option[(Variable, Variable)] = Some((f.domain, f.range))
  
}

//  def apply(dvals: Seq[Double], rvals: Seq[Double]): Function = {
//    /*
//     * TODO: store in Data
//     * expose with iterator, instead of delegating to kids
//     * do we need FunctionData? 2 arrays, or interleave like tuple?
//     *   otherwise each can be an IndexFunction
//     * Seq of pairs, zip, facilitate iteration over pairs
//     *   generalize beyond a pair of doubles, (or special case?)
//     *   but then need structure to make sense of numbers
//     *   use byte buffer?
//     *   don't forget unzip
//     * 
//     * Is there a case where we can delegate to kids' data?
//     * iterate into nested functions...
//     * only if we can let kids have multiple samples?
//     * or split Function into 2 IndexFunctions
//     *   not conducive for function iterator
//     * If domain and range scalars can have multiple values in data
//     *   should we expose IndexFunctions in unapply?
//     * 
//     * DomainSet
//     *   as Scalar with multiple values?
//     *   as IndexFunction?
//     *   *as Data?
//     *   could be linear, index math
//     *   RealSet?
//     * RangeSeq, VariableSeq
//     *   could be nested Fs..., can't be Data?
//     *   are these structures needed?
//     *   just a Variable with Seq of Data?
//     * 
//     * Seems like we need to be able to store multiple values in the Data of a Scalar type
//     * In the context of iterating (e.g. math), need to assume that a Scalar has a single value.
//     * This seems to be the same old problem of mixed abstractions.
//     * Can we just use IndexFunction interchangeably when we need such behavior?
//     * 
//     * just put into function data for now
//     * if math see f data, iterate
//     */
//    val domain = Real(dvals)
//    val range = Real(rvals)
//    Function(domain, range)
//  }
  
//
//object IndexFunction {
//  
//  def apply(ds: Array[Double]): IndexFunction = {
//    //TODO: add length to metadata?
//    
//  }
//}