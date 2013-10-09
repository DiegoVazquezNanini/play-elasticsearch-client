package play.modules.elasticsearch

import org.specs2.mutable.Specification
import play.api.libs.ws.Response
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import org.specs2.time.NoTimeConversions
import scala.util.Failure
import scala.concurrent.Awaitable
import play.api.test.Helpers._
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import org.specs2.specification.Scope
import org.specs2.mutable.Around
import org.specs2.execute.AsResult
import org.specs2.execute.Result

object ClientTests extends Specification with NoTimeConversions {

  val testUrl = "http://localhost:9200"

  sequential

  "Client" should {

    br

    "work without supplying a slash in the url" in {
      testClientHealth must throwA[Throwable].not
    }

    "work with supplying a slash in the url" in {
      awaitResult(new Client(testUrl + "/").health) must throwA[Throwable].not
    }

    "have a health method that returns the health of the server" in {
      val result = testClientHealth

      (result \ "cluster_name").as[String] === "elasticsearch"
    }

    "have an apply and index method to access an index" in {
      testClient("test") must beAnInstanceOf[Client#Index]
      testClient.index("test") must beAnInstanceOf[Client#Index]
    }

    br

    "index should" >> {

      "have a create method" >> {

        "that creates an index" in {

          val result = createTestIndex
          result === ()
        }

        "that throws an exception if an index exists" in {

          val futureResponse = testIndex.create

          isException(futureResponse, BAD_REQUEST, testIndexName)
        }
      }

      "have a delete method" >> {

        "that deletes the index" in {
          val result = deleteTestIndex
          result === ()
        }

        "that fails on an unexisting index" in {
          val futureResponse = testIndex.delete

          isException(futureResponse, NOT_FOUND, testIndexName)
        }
      }

      "have an exists method" >> {

        "that returns true for an existing index" in {
          createTestIndex
          existsTestIndex === true
        }

        "that returns false for a non-existing index" in {
          deleteTestIndex
          existsTestIndex === false
        }
      }

      "have an apply method to access a type" in {
        testIndex("test") must beAnInstanceOf[Client#Index#Type]
      }

      "type should" >> {
        "have a put method to add a document to an index and type" in new WithTestIndex {
          val futureResult =
            testType.put(id = "test", doc = Json.obj("test" -> "test"))

          val result = awaitResult(futureResult)
          result === 1
        }
      }
    }

  }

  val testClient = new Client(elasticSearchUrl = testUrl)
  val testIndexName = "indexname"
  val testTypeName = "typename"
  val defaultTimeout = 2.seconds
  def testClientHealth = awaitResult(testClient.health)
  def testIndex = testClient(indexName = testIndexName)
  def testType = testIndex(typeName = testTypeName)
  def createTestIndex = awaitResult(testIndex.create)
  def deleteTestIndex = awaitResult(testIndex.delete)
  def existsTestIndex = awaitResult(testIndex.exists)

  def awaitResult[T](t: Awaitable[T]) =
    Await.result(t, defaultTimeout)

  def isException(futureResponse: Future[_], status: Int, stringInError: String) = {
    val result = Await.ready(futureResponse, defaultTimeout).value
    result must beLike {
      case Some(Failure(ElasticSearchException(status, error))) =>
        error must contain(stringInError)
    }
  }

  abstract class WithTestIndex extends Scope with Around {
    def around[T: AsResult](t: => T): Result = {
      createTestIndex
      try {
        AsResult(t)
      } finally {
        deleteTestIndex
      }
    }
  }
}