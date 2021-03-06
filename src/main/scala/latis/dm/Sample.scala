package latis.dm

/**
 * This Variable represents a single sample of a Function of the same type.
 * As a pair of domain and range "values", it is a special kind of Tuple.
 */
class Sample(val domain: Variable, val range: Variable) extends AbstractTuple(List(domain,range))


object Sample {
  def apply(domain: Variable, range: Variable): Sample = new Sample(domain, range)

  def apply(vars: Seq[Variable]): Sample = {
    if (vars.length != 2) throw new RuntimeException("Sample must be constructed from two Variables")
    new Sample(vars(0), vars(1))
  }

  /**
   * Expose the domain and range components of the Sample as a pair.
   */
  def unapply(sample: Sample): Option[(Variable, Variable)] = {
    if (sample != null && sample.domain != null && sample.range != null) Some((sample.domain, sample.range))
    else None
  }
}
