package latis.util

import latis.ops.Operation
import latis.ops.filter.Selection
import latis.ops.filter.FirstFilter
import latis.ops.filter.LastFilter
import java.util.Date
import latis.time.Time

object OperationsValidator {
  
  /**
   * Throw an exception if the given Operations don't constrain the time range
   * to the given limit (in milliseconds).
   */
  def validateTimeRange(ops: Seq[Operation], maxTimeRange: Long): Unit = {
    //TODO: make sure min < max, valid format, ...?
    
    var min: Option[Long] = None
    var max: Long = new Date().getTime  //default to now
    var hasFirst: Boolean = false
    var hasLast: Boolean = false
    
    ops foreach {
      case Selection("time", op, time) => 
        if (op.contains(">")) min = Some(Time.isoToJava(time))
        if (op.contains("<")) max = Time.isoToJava(time)
      case ff: FirstFilter => hasFirst = true
      case lf: LastFilter => hasLast = true
      case _ =>
    }
    
    if (!(hasFirst || hasLast)) min match {
      case None => throw new UnsupportedOperationException("This request requires a minimum time selection.")
      case Some(t0) => if ((max - t0) > maxTimeRange)
        throw new UnsupportedOperationException(s"The time selection exceeds the time range limit of ${maxTimeRange} ms.")
    }

  }
}