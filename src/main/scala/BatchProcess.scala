package com.feye.batchprocessor

import collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.annotation.tailrec
import org.slf4j._
import java.util.concurrent.TimeoutException

//Control algorithm for how much data will be batched up with next query
abstract class Incrementer //Not sealed so clients can write their own Incrementers
case object ConstantIncrementer extends Incrementer
case object UnitIncrementer extends Incrementer
case object ExpIncrementer extends Incrementer

abstract class BatchProcess[T,U,V] { 
  
  def produce(elem : T) : Future[U]
  
  def consume(elem : U) : V
  
  private val LOGGER = LoggerFactory.getLogger(this.getClass)
  
  private final def produceAll(in : Iterator[T], size : Int)(implicit ec : ExecutionContext) : Future[Seq[U]] = { 
    val arr = ArrayBuffer.empty[Future[U]]
    while(in.hasNext && arr.length < size) { 
      arr.append(produce(in.next))
    }
    Future.sequence(arr)
  }
  
  private final def consumeAll(first : Seq[U], out : ArrayBuffer[V]) : Unit = { 
    first.foreach(elem => out.append(consume(elem)))
  }
  
  def incrBatchSize(currSize : Int, incType : Incrementer, hit :Boolean) : Int = incType match { 
    case ConstantIncrementer => currSize
    case UnitIncrementer => if (!hit) currSize + 1 else if (currSize == 1) currSize else currSize - 1
    case ExpIncrementer => if (!hit) currSize*2 else if (currSize == 1) currSize else currSize - 1 
  }
  
  @tailrec
  final def process(in : Iterator[T], first : Seq[U], out : ArrayBuffer[V], nextSize : Int, incType : Incrementer, timeout : Int)(implicit ec : ExecutionContext) : ArrayBuffer[V] = { 
    
    LOGGER.debug(s"entering next batch of size $nextSize")
    
    val nextSeq = produceAll(in, nextSize)
    consumeAll(first, out)
    
    //Tail Recurse
    if (in.isEmpty && first.isEmpty) out //Last future never looks
    else if (nextSeq.isCompleted) process(in, nextSeq.value.get.get, out, incrBatchSize(nextSize, incType, true), incType, timeout)
    else { 
      val ns = try { 
	Await.result(nextSeq, timeout seconds)
      } catch { 
	case ex : TimeoutException => { 
	  LOGGER.warn("batch producer timed out")
	  Seq.empty[U]
	}
      }
      process(in, ns, out, incrBatchSize(nextSize, incType, false), incType, timeout)
    }
  }
  
  final def run(in : Iterator[T], start : Int = 1, incType : Incrementer = UnitIncrementer, timeout : Int = 10)(implicit ec : ExecutionContext) : ArrayBuffer[V] = { 
    LOGGER.info("starting batch process")
    process(in, Seq.empty[U], ArrayBuffer.empty[V], start, incType, timeout)
  }
}
