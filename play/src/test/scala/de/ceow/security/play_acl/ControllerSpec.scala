package de.ceow.security.play_acl

import de.ceow.security.play_acl.TestDefinition.ExampleController
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results
import play.api.test._
import play.api.{Application, Configuration, Mode}

class ControllerSpec extends PlaySpecification with Results {

    def controller()(implicit app: Application): ExampleController = app.injector.instanceOf[ExampleController]

    def sessionUsernameKey()(implicit app: Application): String = app.injector.instanceOf[Configuration].get[String]("session.username")

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
        val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "admin")
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
        val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "admin")
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
        val request = FakeRequest(GET, "/withAclAction").withSession(sessionUsernameKey -> "admin")
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
        val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "user")
        val result = call(controller.withProtectedAction(), request)

        contentAsString(result) must be equalTo "Unauthorized"
      }

      "returns result when user is authenticated" in new WithApplication(appWithConfig) {

        implicit val mat = app.materializer
        val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "admin")
        val result = call(controller.withProtectedAction(), request)

        contentAsString(result) must be equalTo "OK"
      }

      "handle withProtectedResource and" in {

        "calls onUnauthorized when not logged in" in new WithApplication(appWithConfig) {

          implicit val mat = app.materializer
          val request = FakeRequest(GET, "/")
          val result = call(controller.withProtectedResourceAction(3), request)
          val result2 = call(controller.withProtectedResourceAction, request)

          contentAsString(result2) must be equalTo "Unauthorized"
        }

        "calls onUnauthorized when logged in" in new WithApplication(appWithConfig) {

          implicit val mat = app.materializer
          val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "user")
          val result = call(controller.withProtectedResourceAction(1), request)
          val result2 = call(controller.withProtectedResourceAction, request)

          contentAsString(result) must be equalTo "Unauthorized"
          contentAsString(result2) must be equalTo "Unauthorized"
        }

        "returns result when user is authenticated" in new WithApplication(appWithConfig) {

          implicit val mat = app.materializer
          val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "admin")
          val result = call(controller.withProtectedResourceAction(2), request)
          val result2 = call(controller.withProtectedResourceAction, request)

          contentAsString(result) must be equalTo "OK user"
          contentAsString(result2) must be equalTo "OK user"
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
          val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "user")
          val result = call(controller.withProtectedAclAction(), request)

          contentAsString(result) must be equalTo "Unauthorized"
        }

        "returns result when user is authenticated" in new WithApplication(appWithConfig) {

          implicit val mat = app.materializer
          val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "admin")
          val result = call(controller.withProtectedAclAction(), request)

          contentAsString(result) must be equalTo "OK"
        }
      }


      "handle withProtectedResource and" in {

        "calls onUnauthorized when not logged in" in new WithApplication(appWithConfig) {

          implicit val mat = app.materializer
          val request = FakeRequest(GET, "/")
          val result = call(controller.withProtectedAclResourceAction(3), request)
          val result2 = call(controller.withProtectedAclResourceAction, request)

          contentAsString(result) must be equalTo "Unauthorized"
          contentAsString(result2) must be equalTo "Unauthorized"
        }

        "calls onUnauthorized when logged in" in new WithApplication(appWithConfig) {

          implicit val mat = app.materializer
          val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "user")
          val result = call(controller.withProtectedAclResourceAction(1), request)
          val result2 = call(controller.withProtectedAclResourceAction, request)

          contentAsString(result) must be equalTo "Unauthorized"
          contentAsString(result2) must be equalTo "Unauthorized"
        }

        "returns result when user is authenticated" in new WithApplication(appWithConfig) {

          implicit val mat = app.materializer
          val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "admin")
          val result = call(controller.withProtectedAclResourceAction(2), request)
          val result2 = call(controller.withProtectedAclResourceAction, request)

          contentAsString(result) must be equalTo "OK user"
          contentAsString(result2) must be equalTo "OK user"
        }
      }

    }
  }

	"Example Async Controller implementation" should {

		"handle withAuth and" in {
			"calls onUnauthorized when not logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/")
				val result = call(controller.withAuthActionAsync(), request)

				contentAsString(result) must be equalTo "Unauthenticated"
			}

			"returns result when user is authenticated" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "admin")
				val result = call(controller.withAuthActionAsync(), request)

				contentAsString(result) must be equalTo "OK admin"
			}
		}

		"handle withUser and" in {

			"returns guest when not logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/")
				val result = call(controller.withUserActionAsync(), request)
				contentAsString(result) must be equalTo "OK guest"
			}

			"returns result when user is authenticated" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "admin")
				val result = call(controller.withUserActionAsync(), request)

				contentAsString(result) must be equalTo "OK admin"
			}
		}

		"handle withAcl and" in {

			"returns guest when not logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/withAclAction")
				val result = call(controller.withAclActionAsync(), request)
				contentAsString(result) must be equalTo "OK"
			}

			"returns result when user is authenticated" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/withAclAction").withSession(sessionUsernameKey -> "admin")
				val result = call(controller.withAclActionAsync(), request)

				contentAsString(result) must be equalTo "OK"
			}
		}

		"handle withProtected and" in {

			"calls onUnauthorized when not logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/")
				val result = call(controller.withProtectedActionAsync(), request)

				contentAsString(result) must be equalTo "Unauthorized"
			}

			"calls onUnauthorized when logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "user")
				val result = call(controller.withProtectedActionAsync(), request)

				contentAsString(result) must be equalTo "Unauthorized"
			}

			"returns result when user is authenticated" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "admin")
				val result = call(controller.withProtectedActionAsync(), request)

				contentAsString(result) must be equalTo "OK"
			}
		}

		"handle withProtectedResource and" in {

			"calls onUnauthorized when not logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/")
				val result = call(controller.withProtectedResourceActionAsync(3), request)

				contentAsString(result) must be equalTo "Unauthorized"
			}

			"calls onUnauthorized when logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "user")
				val result = call(controller.withProtectedResourceActionAsync(1), request)

				contentAsString(result) must be equalTo "Unauthorized"
			}

			"returns result when user is authenticated" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "admin")
				val result = call(controller.withProtectedResourceActionAsync(2), request)

				contentAsString(result) must be equalTo "OK user"
			}
		}

		"handle withProtectedAcl and" in {

			"calls onUnauthorized when not logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/")
				val result = call(controller.withProtectedAclActionAsync(), request)

				contentAsString(result) must be equalTo "Unauthorized"
			}

			"calls onUnauthorized when logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "user")
				val result = call(controller.withProtectedAclActionAsync(), request)

				contentAsString(result) must be equalTo "Unauthorized"
			}

			"returns result when user is authenticated" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "admin")
				val result = call(controller.withProtectedAclActionAsync(), request)

				contentAsString(result) must be equalTo "OK"
			}
		}


		"handle withProtectedResource and" in {

			"calls onUnauthorized when not logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/")
				val result = call(controller.withProtectedAclResourceActionAsync(3), request)

				contentAsString(result) must be equalTo "Unauthorized"
			}

			"calls onUnauthorized when logged in" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "user")
				val result = call(controller.withProtectedAclResourceActionAsync(1), request)

				contentAsString(result) must be equalTo "Unauthorized"
			}

			"returns result when user is authenticated" in new WithApplication(appWithConfig) {

				implicit val mat = app.materializer
				val request = FakeRequest(GET, "/").withSession(sessionUsernameKey -> "admin")
				val result = call(controller.withProtectedAclResourceActionAsync(2), request)

				contentAsString(result) must be equalTo "OK user"
			}
		}

	}
}
