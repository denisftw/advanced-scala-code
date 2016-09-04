package arm



/**
  * Created by denis on 9/2/16.
  */
object ArmUtils {

  import java.io.Closeable
  def usingCloseable[A](closeable: Closeable)(block: => A): A = {
    try {
      block
    } finally {
      closeable.close()
    }
  }
  def using[A, C <: { def close() }](closeable: C)(block: => A): A = {
    try {
      block
    } finally {
      closeable.close()
    }
  }
}
