package com.feye.batchprocessor

import org.specs2.mutable.Specification
import scala.concurrent.Future
import org.slf4j._

class BatchProcessTest extends Specification { 
  
  "The batch process library" should { 
    
    "process strings to find their length" in { 
      
      object BatchStringToInt extends BatchProcess[String,String,Int] { 
	def consume(elem : String) : Int = elem.length	
	def produce(elem : String) : Future[String] = Future { elem }
      }
      
      val s = "the quick brown fox jumped over the lazy dog hooray!!!"
      val in = s.split(" ").toIterator
      val sumScore = BatchStringToInt.run(in).foldLeft(0)(_ + _)
      
      sumScore mustEqual 45
    }
    
    "process strings with inserted delays" in { 
      
      object BatchStringToInt extends BatchProcess[String,String,Int] { 
	def consume(elem : String) : Int = { 
	  Thread.sleep(100)
	  elem.length	
	}	
	def produce(elem : String) : Future[String] = Future { 
	  Thread.sleep(300)
	  elem 
	}
      }
      
      val s = "the quick brown fox jumped over the lazy dog hooray!!! this is just a test we are seeing if it works."
      val in = s.split(" ").toIterator
      val sumScore = BatchStringToInt.run(in, incType=ExpIncrementer).reduce(_ + _)
      
      sumScore mustEqual 81
    }
    
    "be able to overwrite incrementers" in {
      
      case object TenXIncrementer extends Incrementer
      
      object BatchStringToInt extends BatchProcess[String,String,Int] { 
	def consume(elem : String) : Int = { 
	  elem.length
	}	
	def produce(elem : String) : Future[String] = Future { elem } map { result => result + "YA" }
	override def incrBatchSize(currSize : Int, incType : Incrementer, hit :Boolean) : Int = incType match { 
	  case TenXIncrementer => if (!hit) currSize + 1 else if (currSize == 1) currSize else currSize - 1
	}
      }
      
      val s = "the quick brown fox jumped over the lazy dog hooray!!! this is just a test we are seeing if it works."
      val in = s.split(" ").toIterator
      val sumScore = BatchStringToInt.run(in, incType=TenXIncrementer).reduce(_ + _)
      
      sumScore mustEqual 123
    }
    
    "not throw an error if batch timeout is exceeded" in { 
      object BatchStringToInt extends BatchProcess[String,String,Int] { 
	def consume(elem : String) : Int = { 
	  elem.length	
	}	
	def produce(elem : String) : Future[String] = Future { 
	  Thread.sleep(2000)
	  elem 
	}
      }
      
      val s = "the quick brown fox jumped over the lazy dog hooray!!! this is just a test we are seeing if it works."
      val in = s.split(" ").toIterator
      val sumScore = BatchStringToInt.run(in, incType=ExpIncrementer, timeout=1).foldLeft(0)(_ + _)
      
      sumScore mustEqual 0
    }
    
  }
  
}

