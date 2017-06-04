package de.ceow.security.play_acl

import de.ceow.security.acl._
import play.api.mvc._

/**
 * Play Security object
 * wraps play.api.mvc.Security
 */
trait AclSecurity[I <: Identity] {

	/**
	 * will return a Option[Identity]
	 */
	def userByUsername(username: String)(implicit request: RequestHeader): Option[I]

	/**
	 * will return a Identity which represents a guest user
	 */
	def guestUser: I

	/**
	 * will return a list of defined Roles
	 */
	def roles: List[Role]

	/**
	 * will return a Role which represent the guest role
	 */
	def guestRole: Role

	/**
	 * will return the username / email from the session
	 */
	def username(request: RequestHeader) = request.session.get(Security.username)

	/**
	 * method, what happens when a user is not authenticated or
	 * the ACL declines the user to access
	 *
	 * @param request the given requets
	 * @return
	 */
	def onUnauthenticated(request: RequestHeader): Result

	/**
	 * method, which is called, when a user are not allowed to see this page
	 *
	 * @param request the given request
	 * @return
	 */
	def onUnauthorized(request: RequestHeader): Result

	/**
	 * this method will check if a user is authenticated and applies it's username/email
	 * to the action.
	 * If the user is not authenticated the result of "onUnauthenticated" is returned
	 *
	 * def myAction = withAuth { username ⇒ implicit request
	 * 		// Ok(username)
	 * }
	 */
	def withAuth(f: ⇒ String ⇒ Request[AnyContent] ⇒ Result) = {

		Security.Authenticated(username, onUnauthenticated) { user ⇒

			Action(request ⇒ f(user)(request))
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
	def withUser(f: I ⇒ Request[AnyContent] ⇒ Result): Action[AnyContent] = Action { implicit request ⇒

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
	def withAcl(action: Acl ⇒ Request[AnyContent] ⇒ Result): Action[AnyContent] = withUser{ user ⇒ implicit request ⇒

		action(Acl(roles, user))(request)
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
	def withProtectedAcl(resource: Resource, privilege: Privilege)(action: Acl ⇒ Request[AnyContent] ⇒ Result): Action[AnyContent] = {

		withProtectedAcl(resource, privilege, () ⇒ None){obj ⇒ action}
	}

	/**
	  * this method will check if the user is allowed for resource and privilege on
	  * specific object and applies an instance of Acl to the action.
	  * If the Acl check will fail the result of "onUnauthorized" is returned
	  *
	  * def myAction = withProtectedAcl(myResource, myPrivilege, () => MyObject) { Acl: Acl[I] ⇒ implicit request
	  * 		// Ok(Acl.observerIdentity.roles.toString)
	  * }
	  */
	def withProtectedAcl[O <: AclObject](
		resource: Resource,
		privilege: Privilege,
		objectToCheck: () ⇒ Option[O]
	)(f: Option[O] ⇒ Acl ⇒ Request[AnyContent] ⇒ Result): Action[AnyContent] = withAcl { implicit acl: Acl ⇒ implicit request ⇒

		val obj = objectToCheck()
		if (acl.isAllowed(resource, privilege, obj)) {

			f(obj)(acl)(request)
		} else {

			onUnauthorized(request)
		}
	}

	/**
	  * this method will check if the user is allowed for resource and privilege on
	  * specific object and applies an instance of Acl to the action.
	  * If the Acl check will fail the result of "onUnauthorized" is returned
	  *
	  * def myAction = withProtectedAcl(myResource, myPrivilege, acl: Acl => MyObject) { Acl: Acl[I] ⇒ implicit request
	  * 		// Ok(Acl.observerIdentity.roles.toString)
	  * }
	  */
	def withProtectedAcl[O <: AclObject](
		resource: Resource,
		privilege: Privilege,
		objectToCheck: Acl ⇒ Option[O]
	)(f: Option[O] ⇒ Acl ⇒ Request[AnyContent] ⇒ Result): Action[AnyContent] = withAcl { acl: Acl ⇒ implicit request ⇒

		val obj = objectToCheck(acl)
		if (acl.isAllowed(resource, privilege, obj)) {

			f(obj)(acl)(request)
		} else {

			onUnauthorized(request)
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
	def withProtected(resource: Resource, privilege: Privilege)(action: Request[AnyContent] ⇒ Result): Action[AnyContent] = {

		withProtectedAcl(resource, privilege) { acl: Acl ⇒ action}
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
	def withProtected[O <: AclObject](
		resource: Resource,
		privilege: Privilege,
		objectToCheck: () ⇒ Option[O]
	)(action: Option[O] ⇒ Request[AnyContent] ⇒ Result): Action[AnyContent] = {

		withProtectedAcl(resource, privilege, objectToCheck) {

			obj: Option[O] ⇒ implicit acl: Acl ⇒ action(obj)
		}
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
	def withProtected[O <: AclObject](
		resource: Resource,
		privilege: Privilege,
		objectToCheck: Acl ⇒ Option[O]
	)(action: Option[O] ⇒ Request[AnyContent] ⇒ Result): Action[AnyContent] = {

		withProtectedAcl(resource, privilege, objectToCheck) {

			obj: Option[O] ⇒ implicit acl: Acl ⇒ action(obj)
		}
	}

}
