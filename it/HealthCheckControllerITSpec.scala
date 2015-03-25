import ithelpers.{TestConfiguration }
import play.api.test._
import play.api.mvc._
import play.api.Play.current
import play.api.libs.ws._

object HealthCheckControllerITSpec extends PlaySpecification
  with Results with TestConfiguration with TestHelpers {

  val port = 9000
  val url = s"http://localhost:$port/hc"

  "GET to /hc" should {
    "return status=OK" in new WithServer(fakeApp, port) {
      val response = await(WS.url(url).get())
      response.status must equalTo(OK)
    }
  }
}
