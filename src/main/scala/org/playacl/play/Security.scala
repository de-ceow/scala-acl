package org.playacl.play

import play.api.mvc._
import org.playacl.{Identity, Acl, Role, Resource, Privilege}

/**
 * Play Security object
 * wraps play.api.mvc.Security
 */
trait Security[I <: Identity, R <: Role] extends Auth {

	/**
	 * will return a Option[Identity]
	 */
	def userByUsername(username: String): Option[I]

	/**
	 * will return a Identity which represents a guest user
	 */
	def guestUser: I

	/**
	 * will return a list of defined Roles
	 */
	def roles: List[R]

	/**
	 * will return a Role which represent the guest role
	 */
	def guestRole: R

	/**
	 * this method will check if the user is authenticated and applies the
	 * instance of Identity to the action.
	 * If the user is not authenticated the result of "onUnauthorized" is returned
	 *
	 * def myAction = withUser { user: I => implicit request
	 * 		// Ok(user.roles.toString)
	 * }
	 */
	def withUser(f: I => Request[AnyContent] => Result) = withAuth { username => implicit request =>

		userByUsername(username).map { user =>
			f(user)(request)
		}.getOrElse(onUnauthorized(request))
	}

	/**
	 * this method will check if the user is authenticated and applies an
	 * instance of Acl to the action.
	 * If the user is not authenticated the result of "onUnauthorized" is returned
	 *
	 * def myAction = withAcl { Acl: Acl[I] => implicit request
	 * 		// Ok(Acl.observerIdentity.roles.toString)
	 * }
	 */
	def withAcl(f: Acl[I] => Request[AnyContent] => Result) = Action { implicit request =>

		val user = userByUsername(this.username(request).getOrElse("")).getOrElse(guestUser)
		val Acl = new Acl[I](roles, user)

		f(Acl)(request)
	}

	/**
	 * this method will check if the user is authenticated and also check
	 * if the user is allowed for given resource and privilege and applies an
	 * instance of Acl to the action.
	 * If the user is not authenticated or the Acl check will fail the result
	 * of "onUnauthorized" is returned
	 *
	 * def myAction = withProtectedAcl(myResource, myPrivilege) { Acl: Acl[I] => implicit request
	 * 		// Ok(Acl.observerIdentity.roles.toString)
	 * }
	 */
	def withProtectedAcl(resource: Resource, privilege: Privilege)(f: Acl[I] => Request[AnyContent] => Result) = Action { implicit request =>

		val user = userByUsername(this.username(request).getOrElse("")).getOrElse(guestUser)
		val Acl = new Acl[I](roles, user)

		if (Acl.isAllowed(resource, privilege))

			f(Acl)(request)
		else
			onUnauthorized(request)
	}

	/**
	 * this method will check if the user is authenticated and also check
	 * if the user is allowed for given resource and privilege.
	 * If the user is not authenticated or the Acl check will fail the result
	 * of "onUnauthorized" is returned
	 * def myAction = withProtected(myResource, myPrivilege) {implicit request
	 * 		// Ok("done")
	 * }
	 */
	def withProtected(resource: Resource, privilege: Privilege)(f: Request[AnyContent] => Result) = withAuth { username => implicit request =>

		val user = userByUsername(username).getOrElse(guestUser)
		val Acl = new Acl[I](roles, user)

		if (Acl.isAllowed(resource, privilege))

			f(request)
		else
			onUnauthorized(request)
	}
}
