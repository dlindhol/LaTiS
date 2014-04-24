package latis.util

/**
 * An Iterator that looks ahead and caches the next sample.
 * This makes it easier to know when we are done, especially 
 * when we are filtering. The original source may have another 
 * sample but this will keep looking for the next valid sample.
 * You can also "peek" at the next sample without advancing.
 */
abstract class PeekIterator[T >: Null] extends Iterator[T] {
  //Note, the bound on Null allows us to return null for generic type T.
  
  /**
   * Cached next value. Will be null if there is no more elements.
   */
  private var _next: T = null

  /**
   * Take a look at the next sample without advancing to it.
   */
  final def peek: T = _next
  
  /**
   * Use this lazy val so initialization will happen when this is first asked
   * if it is initialized.
   */
  private lazy val _initialized: Boolean = {
    _next = getNext
    true
  }
  
  /**
   * True if there is a cached next value.
   * The first call to this will cause the first value to be accessed and cached.
   */
  final def hasNext: Boolean = _initialized && _next != null

    
  /**
   * Return the 'next' value and cache the next 'next' value
   * to effectively advance to the next sample.
   */
  final def next: T = {
    _initialized //make sure we have cached the first value
    val current = _next
    _next = getNext //TODO: get next value to cache asynchronously?
    //_index += 1 //increment the current index
    current
  }
  
  /**
   * Responsible for getting the next transformed item 
   * or null if there are no more valid items.
   * This should keep trying until a valid sample is found 
   * or it hits the end of the original iterator.
   */
  protected def getNext: T
}

