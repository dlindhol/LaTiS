package latis.ops

import latis.dm.Function
import latis.metadata.Metadata

class DropOperation(val n: Int) extends Operation {
  override def applyToFunction(function: Function) = {
    //Assume we can hold this all in memory.
    
    //get data without first n samples
    val samples = function.iterator.drop(n).toList
    
    // drop does not update length metadata
    // since it doesn't make sense for infinite streams
    
    //make the new function
    samples.length match {
      case 0 => Some(Function(function.getDomain, function.getRange, Iterator.empty)) //empty Function with type of original
      case _ => Some(Function(samples))
    }
  }
}

object DropOperation extends OperationFactory {
  
  override def apply(args: Seq[String]): DropOperation = {
    if (args.length > 1) throw new UnsupportedOperationException("The DropOperation accepts only one argument")
    try {
      DropOperation(args.head.toInt)
    } catch {
      case e: NumberFormatException => throw new UnsupportedOperationException("The DropOperation requires an integer argument")
    }
  }
    
  def apply(n: Int): DropOperation = new DropOperation(n)
}