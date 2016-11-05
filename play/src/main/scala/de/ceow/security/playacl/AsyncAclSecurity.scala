package de.ceow.security.playacl

import de.ceow.security.acl._
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * Play Security object
 * wraps play.api.mvc.Security
 */
trait AsyncAclSecurity[I <: Identity] extends AclSecurity[I] {

	/**
	 * method, which is called, when a user are not allowed to see this page
 *
	 * @param request the given requets
	 * @return
	 */
	def onUnauthorizedAsync(request: RequestHeader): Future[Result] = Future(onUnauthorized(request))

	/**
	 * this method will check if a user is authenticated and applies it's username/email
	 * to the action.
	 * If the user is not authenticated the result of "onUnauthenticated" is returned
	 *
	 * def myAction = withAuth { username ⇒ implicit request
	 * 		// Ok(username)
	 * }
	 */
	def withAuthAsync(f: ⇒ String ⇒ Request[AnyContent] ⇒ Future[Result]) = {

		Security.Authenticated(username, onUnauthenticated) { user ⇒

			Action.async(request ⇒ f(user)(request))
		}
	}

	/**
	 * this method will check if the user is authenticated and applies the
	 * instance of Identity to the action.
	 * If the user is not authenticated the guest user will be returned
	 *
	 * def myAction = withUser { user: I ⇒ implicit request
	 * 		// Ok(user.roles.toString)
	 * }
	 */
	def withUserAsync(f: I ⇒ Request[AnyContent] ⇒ Future[Result]): Action[AnyContent] = Action.async { implicit request ⇒

		val user = userByUsername(this.username(request).getOrElse("")).getOrElse(guestUser)
		f(user)(request)
	}

	/**
	 * this method will return the logged in user or a guest user instance and applies an
	 * instance of Acl to the action.
	 *
	 * def myAction = withAcl { Acl: Acl[I] ⇒ implicit request
	 * 		// Ok(Acl.observerIdentity.roles.toString)
	 * }
	 */
	def withAclAsync(action: Acl ⇒ Request[AnyContent] ⇒ Future[Result]): Action[AnyContent] = withUserAsync { user ⇒ implicit request ⇒

		action(new Acl(roles, user))(request)
	}

	/**
	 * this method will check if the user is allowed for given resource and privilege and applies an
	 * instance of Acl to the action.
	 * If the Acl check will fail the result of "onUnauthorized" is returned
	 *
	 * def myAction = withProtectedAcl(myResource, myPrivilege) { Acl: Acl[I] ⇒ implicit request
	 * 		// Ok(Acl.observerIdentity.roles.toString)
	 * }
	 */
	def withProtectedAclAsync(resource: Resource, privilege: Privilege)(action: Acl ⇒ Request[AnyContent] ⇒ Future[Result]): Action[AnyContent] = {

		withProtectedAclAsync(resource, privilege, () ⇒ None){obj ⇒ action}
	}

	/**
	  * this method will check if the user is allowed for resource and privilege on
	  * specific object and applies an instance of Acl to the action.
	  * If the Acl check will fail the result of "onUnauthorized" is returned
	  *
	  * def myAction = withProtectedAcl(myResource, myPrivilege, () = MyObject) { Acl: Acl[I] ⇒ implicit request
	  * 		// Ok(Acl.observerIdentity.roles.toString)
	  * }
	  */
	def withProtectedAclAsync[O <: AclObject](
		resource: Resource,
		privilege: Privilege,
		objectToCheck: () ⇒ Option[O]
	)(f: Option[O] ⇒ Acl ⇒ Request[AnyContent] ⇒ Future[Result]): Action[AnyContent] = withAclAsync { acl: Acl ⇒ implicit request ⇒

		val obj = objectToCheck()
		acl.isAllowed(resource, privilege, obj) match {

			case true ⇒ f(obj)(acl)(request)
			case false ⇒ onUnauthorizedAsync(request)
		}
	}

	/**
	 * This method will check if the user is allowed for given resource and privilege
	 * If the Acl check will fail the result of "onUnauthorized" is returned
	 *
	 * def myAction = withProtected(myResource, myPrivilege) {implicit request
	 * 		// Ok("done")
	 * }
	 */
	def withProtectedAsync(resource: Resource, privilege: Privilege)(action: Request[AnyContent] ⇒ Future[Result]): Action[AnyContent] = {

		withProtectedAclAsync(resource, privilege) { acl: Acl ⇒ action}
	}

	/**
	 * This method will check if the user is allowed for given resource and privilege
  	 * on specific object
	 * If the Acl check will fail the result of "onUnauthorized" is returned
	 *
	 * def myAction = withProtected(myResource, myPrivilege) {implicit request
	 * 		// Ok("done")
	 * }
	 */
	def withProtectedAsync[O <: AclObject](
		resource: Resource,
		privilege: Privilege,
		objectToCheck: () ⇒ Option[O]
	)(action: Option[O] ⇒ Request[AnyContent] ⇒ Future[Result]): Action[AnyContent] = {

		withProtectedAclAsync(resource, privilege, objectToCheck) {

			obj: Option[O] ⇒ implicit acl: Acl ⇒ action(obj)
		}
	}

}
