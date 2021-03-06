package latis.metadata

/**
 * Just name/value pairs, for now.
 */
trait Metadata {
  def getProperties: Map[String,String]
  
  def get(key: String): Option[String]
  
  def getOrElse(key: String, default: => String): String
  
  def apply(key: String): String = get(key) match {
    case Some(s) => s
    case None => null //TODO: error? default?
  }
  
  /**
   * Add a property returning a new Metadata object.
   * Replace if it already exists.
   */
  def +(kv: (String,String)): Metadata = Metadata(getProperties + kv)
  //TODO: MetadataBuilder so we can keep adding properties without making a new object each time.
  def ++(md: Metadata): Metadata = Metadata(getProperties ++ md.getProperties)
  
  def has(key: String): Boolean
  
  /**
   * If the Metadata does not have a "name" property, set it.
   * Otherwise, add an "alias".
   */
  def addName(name: String): Metadata = get("name") match {
    //Note, this does not prevent duplicates, which shouldn't hurt.
    case Some(_) => get("alias") match {
      case Some(a) => this + ("alias" -> s"$a,$name") //append to list of aliases
      case None    => this + ("alias" -> name)        //add alias
    }
    case None => this + ("name" -> name)
  }
  
  /**
   * Replace name.
   */
  def setName(name: String): Metadata = this + ("name" -> name)
  
  def isEmpty: Boolean = getProperties.isEmpty
  def nonEmpty: Boolean = getProperties.nonEmpty
   
  override def toString(): String = getProperties.toString
}


object Metadata {
  
  import scala.collection.immutable
  import scala.collection.Map
  
  val empty = EmptyMetadata
  
  /**
   * Convenience for providing Variables with a name.
   */
  def apply(name: String): Metadata = {
    //TODO: make sure this is a valid name
    val props = immutable.HashMap(("name", name))
    new VariableMetadata(props)
  }
  
  /**
   * Construct Metadata from a Map of properties.
   */
  def apply(properties: Map[String,String]): Metadata = properties match {
    case null => EmptyMetadata
    case _ => if (properties.isEmpty) EmptyMetadata else new VariableMetadata(properties.toMap)
  }
  
  /**
   * Construct Metadata from a single property.
   */
  def apply(property: (String,String)): Metadata = new VariableMetadata(immutable.Map(property))
  
  /**
   * Construct Metadata from a list of properties.
   */
  def apply(properties: (String,String)*): Metadata = new VariableMetadata(immutable.Map(properties: _*))

}
