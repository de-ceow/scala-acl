/**
 * Copyright (c) 2015, Ronald Marske
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may
 * be used to endorse or promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.github.scyks.playacl.play

import com.github.scyks.playacl.{Identity, Role, Resource, Privilege, Acl}
import play.api.mvc._

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
