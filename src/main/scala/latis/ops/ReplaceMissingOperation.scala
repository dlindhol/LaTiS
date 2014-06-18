package latis.ops

import latis.dm.Scalar
import scala.math.ScalaNumericAnyConversions
import latis.dm.Integer
import latis.dm.Real

/**
 * Operation to replace any missing value in a Dataset with another.
 */
class ReplaceMissingOperation(value: ScalaNumericAnyConversions) extends Operation {
  //TODO: ExcludeMissing, filter
  //TODO: apply to text

  override def applyToScalar(scalar: Scalar): Option[Scalar] = scalar match {
    case r: Real if (r.isMissing) => Some(Real(r.getMetadata, value.doubleValue))
    case i: Integer if (i.isMissing) => Some(Integer(i.getMetadata, value.longValue))
    case _ => Some(scalar)
  }

}


object ReplaceMissingOperation extends OperationFactory {
  
  override def apply(args: Seq[String]): ReplaceMissingOperation = {
    if (args.length != 1) throw new UnsupportedOperationException("The ReplaceMissingOperation requires one argument.")
    val v: ScalaNumericAnyConversions = ???
    //TODO: need to know what type to convert string to
    ReplaceMissingOperation(v)
  }
  
  def apply(value: ScalaNumericAnyConversions) = new ReplaceMissingOperation(value)
}