package play.modules.elasticsearch.filter

import play.api.libs.json.JsValue
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.modules.elasticsearch.EnumUtils

/**
 * See http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-filters.html
 */
trait Filter {

  /**
   * Create a filter object that can be used in a compound query.
   */
  def toQueryDSL: JsValue

}

object RangeExecution extends Enumeration {
  val index, fielddata = Value
  val default = index
  implicit val enumReads: Reads[Value] = EnumUtils.enumReads(RangeExecution)
  implicit def enumWrites: Writes[Value] = EnumUtils.enumWrites
}
