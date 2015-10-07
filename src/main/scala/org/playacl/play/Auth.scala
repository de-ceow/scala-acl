package org.playacl.play

import play.api.mvc._

trait Auth {

	/**
	 * will return the username / email from the session
	 */
	def username(request: RequestHeader) = request.session.get(Security.username)

	/**
	 * method, what happens when a user is not authenticated or
	 * the ACL declines the user to access
	 */
	def onUnauthorized(request: RequestHeader) = Results.Redirect("/login")

	/**
	 * this method will check if a user is authenticated and applies it's username/email
	 * to the action.
	 * If the user is not authenticated the result of "onUnauthorized" is returned
	 *
	 * def myAction = withAuth { username => implicit request
	 * 		// Ok(username)
	 * }
	 */
	def withAuth(f: => String => Request[AnyContent] => Result) = {

		Security.Authenticated(username, onUnauthorized) { user =>
			Action(request => f(user)(request))
		}
	}
}