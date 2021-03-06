package latis.ops.agg

import latis.dm.Dataset
import latis.dm.Function
import latis.dm.Sample
import latis.dm.Scalar
import latis.dm.Tuple
import latis.dm.Variable
import latis.metadata.EmptyMetadata
import latis.metadata.Metadata
import latis.util.iterator.PeekIterator

class LeftOuterJoin extends Aggregation { 
  //use case: x -> (a,b) LeftOuterJoin x -> (c,d) => x -> (a,b,c,d) 
  //  where all samples of the initial dataset are kept
  //  using fill values for the second dataset
  /*
   * TODO: implement with resampling? but traversable once problem
   * this could use assumption that both are sorted
   * resampling in general would violate FP since it depends on state of iterator
   * maybe some common chunk of code?
   */
  
  def aggregate(ds1: Dataset, ds2: Dataset): Dataset = {    
    val (f1, f2, it1, it2) = (ds1, ds2) match {
      case(Dataset(f1 @ Function(it1)), Dataset(f2 @ Function(it2))) => {
        //TODO: what if ranges have same parameter? keep first?
        //make sure the domains are consistent, match on name for now
        if (f1.getDomain.getName != f2.getDomain.getName) {
          val msg = s"Can't join Functions with different domains."
          throw new UnsupportedOperationException(msg)
        }
        (f1, f2, it1, it2)
      }
      case _ => throw new UnsupportedOperationException("LeftOuterJoin expects a Function in each of the Datasets it aggregates.")
    }
    
    //Don't join an empty function
    if (f2.isEmpty) return ds1
    if (f1.isEmpty) return ds2
    
    //need domain and range types for new Function
    val dtype = f1.getDomain
    val rtype = Tuple(f1.getRange, f2.getRange)
    
    //Make PeekIterator for the second dataset so we can look ahead
    val pit2 = PeekIterator(it2)
    
    //make iterator to return joined samples
    val samples = new PeekIterator[Sample]() {
      def getNext = {
        if (it1.hasNext) {
          val s1 = it1.next  //next sample from first dataset
          val d1 = s1.domain.asInstanceOf[Scalar]
          //val s2 = pit2.peek //next sample from second dataset
          
          def getNextMatchingSample: Option[Sample] = {
            //try the next sample without advancing the iterator
            val sample = pit2.peek
            if (sample != null) {
              val d2 = sample.domain.asInstanceOf[Scalar]
              d1 compare d2 match {
                case 0 => Some(sample) //found a match
                case n: Int if (n < 0) => None  //the other is already ahead
                case n: Int if (n > 0) => {
                  //we are ahead of the other, try the next one
                  pit2.next //advance the iterator
                  getNextMatchingSample
                }
              }
            } else None //there are no more samples
          }
          
          val range2 = getNextMatchingSample match {
            case Some(sample) => sample.range
            case None => makeFillVariable(f2.getRange)
          }
          
          val range = Tuple(s1.range, range2)
          Sample(d1,range)
          
        } else null
      }
    }
    
    //TODO: update metadata
    //use metadata from first Dataset for new Dataset
    //Note, dataset metadata must be passed in via the super apply(ds1, ds2, md)
    val fmd = f1.getMetadata
    
    Dataset(Function(dtype, rtype, samples, fmd))
  }
  

  //TODO: util method
  def makeFillVariable(v: Variable): Variable = v match {
    case s: Scalar => Scalar(s.getMetadata, s.getFillValue)
    case Tuple(vars) => Tuple(vars.map(makeFillVariable(_))) //preserve type by keeping members
    case f: Function => Function(f.getDomain, f.getRange, f.getMetadata) //empty Function that preserves type
  }
}

object LeftOuterJoin {
  
  def apply(): LeftOuterJoin = new LeftOuterJoin()
  def apply(ds1: Dataset, ds2: Dataset, md: Metadata = EmptyMetadata): Dataset = new LeftOuterJoin()(ds1, ds2, md)
}