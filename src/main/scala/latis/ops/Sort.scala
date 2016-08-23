package latis.ops

import latis.dm.Function


class Sort extends Operation {
    override def applyToFunction(function: Function) = {
    
    // get samples from function and sort them
    val sortedSamples = VarSort(function.iterator.toSeq)
    
    // update length metadata
    val md = function.getMetadata + ("length" -> sortedSamples.length.toString)
    
    //make the new function
    sortedSamples.length match {
      case 0 => Some(Function(function.getDomain, function.getRange, Iterator.empty, md)) //empty Function with type of original
      case _ => Some(Function(sortedSamples))
    }
  }
}

object Sort extends OperationFactory {
  override def apply: Sort = new Sort
}