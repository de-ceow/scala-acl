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

import _root_.play.api.mvc._
import com.github.scyks.playacl._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Play Security object
 * wraps play.api.mvc.Security
 */
trait AsyncSecurity[I <: Identity] extends Security[I] {

	/**
	 * method, which is called, when a user are not allowed to see this page
	 * @param request the given requets
	 * @return
	 */
	def onUnauthorizedAsync(request: RequestHeader): Future[Result] = Future{onUnauthorized(request)}

	/**
	 * this method will check if a user is authenticated and applies it's username/email
	 * to the action.
	 * If the user is not authenticated the result of "onUnauthenticated" is returned
	 *
	 * def myAction = withAuth { username => implicit request
	 * 		// Ok(username)
	 * }
	 */
	def withAuthAsync(f: => String => Request[AnyContent] => Future[Result]) = {

		Security.Authenticated(username, onUnauthenticated) { user =>

			Action.async(request => f(user)(request))
		}
	}

	/**
	 * this method will check if the user is authenticated and applies the
	 * instance of Identity to the action.
	 * If the user is not authenticated the guest user will be returned
	 *
	 * def myAction = withUser { user: I => implicit request
	 * 		// Ok(user.roles.toString)
	 * }
	 */
	def withUserAsync(f: I => Request[AnyContent] => Future[Result]): Action[AnyContent] = Action.async { implicit request =>

		val user = userByUsername(this.username(request).getOrElse("")).getOrElse(guestUser)
		f(user)(request)
	}

	/**
	 * this method will return the logged in user or a guest user instance and applies an
	 * instance of Acl to the action.
	 *
	 * def myAction = withAcl { Acl: Acl[I] => implicit request
	 * 		// Ok(Acl.observerIdentity.roles.toString)
	 * }
	 */
	def withAclAsync(action: Acl => Request[AnyContent] => Future[Result]): Action[AnyContent] = withUserAsync { user => implicit request =>

		action(new Acl(roles, user))(request)
	}

	/**
	 * this method will check if the user is allowed for given resource and privilege and applies an
	 * instance of Acl to the action.
	 * If the Acl check will fail the result of "onUnauthorized" is returned
	 *
	 * def myAction = withProtectedAcl(myResource, myPrivilege) { Acl: Acl[I] => implicit request
	 * 		// Ok(Acl.observerIdentity.roles.toString)
	 * }
	 */
	def withProtectedAclAsync(resource: Resource, privilege: Privilege)(action: Acl => Request[AnyContent] => Future[Result]): Action[AnyContent] = {

		withProtectedAclAsync(resource, privilege, () => None){obj => action}
	}

	/**
	  * this method will check if the user is allowed for resource and privilege on
	  * specific object and applies an instance of Acl to the action.
	  * If the Acl check will fail the result of "onUnauthorized" is returned
	  *
	  * def myAction = withProtectedAcl(myResource, myPrivilege, () = MyObject) { Acl: Acl[I] => implicit request
	  * 		// Ok(Acl.observerIdentity.roles.toString)
	  * }
	  */
	def withProtectedAclAsync[O <: AclObject](
		resource: Resource,
		privilege: Privilege,
		objectToCheck: () => Option[O]
	)(f: Option[O] => Acl => Request[AnyContent] => Future[Result]): Action[AnyContent] = withAclAsync { acl: Acl => implicit request =>

		val obj = objectToCheck()
		acl.isAllowed(resource, privilege, obj) match {

			case true => f(obj)(acl)(request)
			case false => onUnauthorizedAsync(request)
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
	def withProtectedAsync(resource: Resource, privilege: Privilege)(action: Request[AnyContent] => Future[Result]): Action[AnyContent] = {

		withProtectedAclAsync(resource, privilege) { acl: Acl => action}
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
		objectToCheck: () => Option[O]
	)(action: Option[O] => Request[AnyContent] => Future[Result]): Action[AnyContent] = {

		withProtectedAclAsync(resource, privilege, objectToCheck) {

			obj: Option[O] => implicit acl: Acl => action(obj)
		}
	}

}
