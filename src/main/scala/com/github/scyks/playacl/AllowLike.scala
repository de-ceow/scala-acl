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

package com.github.scyks.playacl

/**
 * Allow like trait
 */
trait AllowLike {

//	/**
//	 * Allow stuff
//	 */
//	class AllowPrivilege(r: Resource, objectToCheck: Option[AclObject]) {
//
//		def to(p: Privilege): Boolean = Acl.isAllowed(r, p, objectToCheck)
//	}
//
//	def allows(r: Resource) = {
//
//		this match {
//			case a: Acl => new AllowPrivilege(r, None)
//			case b: AclObject => new AllowPrivilege(r, Some(b))
//			case _ => new AllowPrivilege(r, None)
//		}
//	}
//
//	def allows(p: Privilege) = {
//
//		this match {
//			case r: Resource => new AllowPrivilege(r, None) to p
//			case _ => throw new Exception("AllowLike is not able to handle the input")
//		}
//	}
//
//	def allows(v: Any) = {
//
//		this match {
//			case r: Resource => new AllowPrivilege(r, None)
//			case _ => throw new Exception("AllowLike is not able to handle the input")
//		}
//	}
}