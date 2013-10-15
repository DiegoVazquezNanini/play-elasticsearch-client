package play.modules.elasticsearch.query

import play.api.libs.json.Reads
import play.api.libs.json.Json
import play.api.libs.json.Writes

/*
 * See http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-term-query.html
 */
case class TermQuery(field: String, value: String, boost: Double = -1.0) extends Query {
  
  def withBoost(boost: Double) : TermQuery =
    copy(boost = boost)
  
  def toQueryDSL =
      if (boost < 0.0)
        Json.obj("term" -> Json.obj(field -> value))
      else 
        Json.obj("term" -> Json.obj(field -> Json.obj("value" -> value, "boost" -> boost)))

}
