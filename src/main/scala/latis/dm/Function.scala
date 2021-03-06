package latis.dm

import latis.data.EmptyData
import latis.data.SampleData
import latis.data.SampledData
import latis.metadata.EmptyMetadata
import latis.metadata.Metadata

import scala.collection.Iterator
import scala.collection.Seq

/**
 * Variable that represents a mapping from one Variable to another.
 */
trait Function extends Variable {
  def getDomain: Variable
  def getRange: Variable
  def getSample: Sample

  //TODO: only applies to SampledFunction
  def getLength: Int //TODO: long?

  //TODO: only applicable to SampledFunction, need to replace lots of pattern matches...
  def iterator: Iterator[Sample]
  def getDataIterator: Iterator[SampleData]

    /**
   * Returns true if iterator.isEmpty returns true,
   * else false. This method is preferred over the
   * other because implementations may be able to
   * make certain optimizations on this method that
   * would not be possible on the other.
   *
   * For example:
   * - calling Function.isEmpty instead of
   * calling Function.iterator.isEmpty may save
   * the function from creating an Iterator
   * instance
   * - sometimes calling Function.iterator.isEmpty
   * may consume an item from the internal iterator.
   * This may be avoidable by calling Function.isEmpty
   */
  def isEmpty: Boolean

//  /**
//   * Datasets from IterableAdapters tend to be "iterable once" in which case this
//   * will return false. If the Data has been realized (memoized) within the dataset
//   * structure, this will return true. This will be the case when the Function is
//   * constructed from a Seq of Samples (as opposed to an Iterator), or the Memoization
//   * operation has been applied to the Dataset (via 'force').
//   */
//  def isTraversableAgain: Boolean = _traversableAgain
//  private var _traversableAgain = false
////TODO: test how this works with SampledData
//  //TODO: add this method to Dataset?

  //evaluate
  def apply(arg: Variable): Option[Variable]
  def compose(g: Function): Function
}

  /*
   * TODO: ContinuousFunction:
   *   apply(domainVal: Variable) => range val
   *   apply(domainSet: Seq[Variable] or Var with SeqData) => SampledFunction
   *   length = -1?
   *   iterator => error
   */

object Function {
  //TODO: make sure samples are sorted!

  def empty: SampledFunction = Function(Naught(), Naught(), Iterator.empty)

  def apply(domain: Variable, range: Variable, md: Metadata = EmptyMetadata, data: SampledData = EmptyData): SampledFunction = {
    new SampledFunction(domain, range, md, data)
  }

  /**
   * Construct a Function from the given template (e.g. model from tsml) and data.
   */
  def apply(template: Function, data: SampledData): SampledFunction = {
    Function(template.getDomain, template.getRange, template.getMetadata, data)
  }

  /**
   * Construct from Iterator of Samples.
   */
  def apply(domain: Variable, range: Variable, sampleIterator: Iterator[Sample], md: Metadata): SampledFunction = {
    SampledFunction(domain, range, sampleIterator, md)
  }
  def apply(domain: Variable, range: Variable, sampleIterator: Iterator[Sample]): SampledFunction = {
    Function(domain, range, sampleIterator, EmptyMetadata)
  }
  def apply(template: Function, sampleIterator: Iterator[Sample]): SampledFunction = {
    Function(template.getDomain, template.getRange, sampleIterator, template.getMetadata)
  }

  /**
   * Construct from Seq of Variable which are assumed to contain their own data.
   */
  def apply(vs: Seq[Variable], md: Metadata): SampledFunction = vs.headOption match {
    case Some(sample: Sample) => SampledFunction(vs.asInstanceOf[Seq[Sample]], md)
    case Some(_) => {
      //make Seq of samples where domain is index
      //TODO: make sure every Variable in the Seq has the same type
      val samples = vs.zipWithIndex.map(s => Sample(Index(s._2), s._1))
      val sample = samples.head
      Function(sample.domain, sample.range, samples.iterator, md)
    }
    case None => Function(Naught(), Naught(), Iterator.empty, md) //Function.empty
  }

  def apply(vs: Seq[Variable]): SampledFunction = Function(vs, EmptyMetadata)
  //TODO: accept Iterable?

  /**
   * Construct from a Seq of domain Variables and a Seq of range Variables.
   */
  def apply(ds: Seq[Variable], rs: Seq[Variable]): SampledFunction = {
    if (ds.length != rs.length) throw new RuntimeException("Domain and range sequences must have the same length.")
    Function((ds zip rs).map(s => Sample(s._1, s._2)))
  }

  /**
   * Construct from a 2D sequence of double values. Assume the first is for a 1D domain.
   */
//  def fromValues(vals: Seq[Seq[Double]]): SampledFunction = Function.fromValues(vals.head, vals.tail: _*)
//TODO: assume index function

  def apply(ds: Seq[Variable], rs: Seq[Variable], md: Metadata): Function = {
    //TODO: assert same length?
//TODO: require SampledData? cacheable
    //TODO: use metadata from Variables
    Function((ds zip rs).map(s => Sample(s._1, s._2)), md)
  }

  /**
   * Construct from a sequence of double values and a sequence of range values for multiple variables.
   */
  def fromValues(dvals: Seq[Double], vals: Seq[Double]*): SampledFunction = {
    val domain = Real(Metadata("domain"))
    val range = vals.length match {
      case 1 => Real(Metadata("range"))
      case n: Int => Tuple((0 until n).map(i => Real(Metadata("real"+i)))) //auto-gen names
    }
    val data = SampledData.fromValues(dvals, vals: _*)

    //get length for metadata
    val md = Metadata(Map("length" -> dvals.length.toString))

    Function(domain, range, md, data)
  }

  /**
   * Expose the Sample Iterator.
   */
  def unapply(f: SampledFunction): Option[Iterator[Sample]] = Some(f.iterator)
  //TODO: only applies to SampledFunction, define it there

}
