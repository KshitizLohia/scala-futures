package example

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

import org.scalatest.{FlatSpec, Matchers}

class ThreadPoolExecutorSpec extends FlatSpec with Matchers {

  behavior of "ThreadPoolExecutor"

  val counter = new AtomicInteger

  def elapsed[T](x: ⇒ T): (Long, T) ={
    val initial = System.nanoTime()
    val result = x
    (System.nanoTime() - initial, result)
  }

  def makeRunnable(n: Int): Runnable = { () ⇒
    Thread.sleep(1000)
    val r = counter.addAndGet(n)
    println(s"Counter is now $r")
  }

  def makeRunnableCrash(n: Int): Runnable = { () ⇒
    Thread.sleep(1000)
    val r = counter.addAndGet(n)
    null.asInstanceOf[String].length()
    println(s"Counter is now $r")
  }

  it should "run several tasks in parallel" in {
    val tpe = Executors.newFixedThreadPool(3)

    counter.set(0)

    val tasks: Seq[Runnable] = (1 to 5).map(makeRunnable)

    tasks.foreach(task ⇒ tpe.submit(task))

    Thread.sleep(4000) // hmmm.
    counter.get shouldEqual 15

    tpe.shutdown()
  }

  it should "be able to schedule tasks after exceptions" in {
    val tpe = Executors.newSingleThreadExecutor()

    counter.set(0)

    val tasks: Seq[Runnable] = (1 to 5).map(makeRunnableCrash) ++ (6 to 7).map(makeRunnable)

    tasks.foreach(task ⇒ tpe.submit(task))

    Thread.sleep(4000) // hmmm.
    counter.get shouldEqual 28

    tpe.shutdown()
  }

}
