package com.github.scyks.playacl

import _root_.play.api.mvc._
import _root_.play.api.test.PlaySpecification
import _root_.play.api.test._
import _root_.play.api.{Mode, Configuration}
import _root_.play.api.inject.guice.GuiceApplicationBuilder
import org.specs2.execute.AsResult

class ControllerSpec extends PlaySpecification with Results {

	val controller = new TestDefinitions.ExampleController()

	def appWithConfig = new GuiceApplicationBuilder()
		.loadConfig(env => Configuration.load(env))
		.in(Mode.Test)
		.build

	"Example Controller implementation" should {

		"handle withAuth and" in {
			"calls onUnauthorized when not logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/")
				val result = call(controller.withAuthAction(), request)

				contentAsString(result) must be equalTo "Unauthenticated"
			}

			"returns result when user is authenticated" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(Security.username -> "admin")
				val result = call(controller.withAuthAction(), request)

				contentAsString(result) must be equalTo "OK admin"
			}
		}

		"handle withUser and" in {

			"returns guest when not logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/")
				val result = call(controller.withUserAction(), request)
				contentAsString(result) must be equalTo "OK guest"
			}

			"returns result when user is authenticated" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(Security.username -> "admin")
				val result = call(controller.withUserAction(), request)

				contentAsString(result) must be equalTo "OK admin"
			}
		}

		"handle withAcl and" in {

			"returns guest when not logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/withAclAction")
				val result = call(controller.withAclAction(), request)
				contentAsString(result) must be equalTo "OK"
			}

			"returns result when user is authenticated" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/withAclAction").withSession(Security.username -> "admin")
				val result = call(controller.withAclAction(), request)

				contentAsString(result) must be equalTo "OK"
			}
		}

		"handle withProtected and" in {

			"calls onUnauthorized when not logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/")
				val result = call(controller.withProtectedAction(), request)

				contentAsString(result) must be equalTo "Unauthorized"
			}

			"calls onUnauthorized when logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(Security.username -> "user")
				val result = call(controller.withProtectedAction(), request)

				contentAsString(result) must be equalTo "Unauthorized"
			}

			"returns result when user is authenticated" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(Security.username -> "admin")
				val result = call(controller.withProtectedAction(), request)

				contentAsString(result) must be equalTo "OK"
			}
		}

		"handle withProtectedResource and" in {

			"calls onUnauthorized when not logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/")
				val result = call(controller.withProtectedResourceAction(3), request)

				contentAsString(result) must be equalTo "Unauthorized"
			}

			"calls onUnauthorized when logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(Security.username -> "user")
				val result = call(controller.withProtectedResourceAction(1), request)

				contentAsString(result) must be equalTo "Unauthorized"
			}

			"returns result when user is authenticated" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(Security.username -> "admin")
				val result = call(controller.withProtectedResourceAction(2), request)

				contentAsString(result) must be equalTo "OK user"
			}
		}

		"handle withProtectedAcl and" in {

			"calls onUnauthorized when not logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/")
				val result = call(controller.withProtectedAclAction(), request)

				contentAsString(result) must be equalTo "Unauthorized"
			}

			"calls onUnauthorized when logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(Security.username -> "user")
				val result = call(controller.withProtectedAclAction(), request)

				contentAsString(result) must be equalTo "Unauthorized"
			}

			"returns result when user is authenticated" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(Security.username -> "admin")
				val result = call(controller.withProtectedAclAction(), request)

				contentAsString(result) must be equalTo "OK"
			}
		}


		"handle withProtectedResource and" in {

			"calls onUnauthorized when not logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/")
				val result = call(controller.withProtectedAclResourceAction(3), request)

				contentAsString(result) must be equalTo "Unauthorized"
			}

			"calls onUnauthorized when logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(Security.username -> "user")
				val result = call(controller.withProtectedAclResourceAction(1), request)

				contentAsString(result) must be equalTo "Unauthorized"
			}

			"returns result when user is authenticated" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(Security.username -> "admin")
				val result = call(controller.withProtectedAclResourceAction(2), request)

				contentAsString(result) must be equalTo "OK user"
			}
		}

	}
}