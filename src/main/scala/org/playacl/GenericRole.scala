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

package org.playacl

case class GenericRole(name: String, inheritedRoles: List[Role]) extends Role {

	/**
	 * defines the unique "bit"-identifier for this role. Keep in mind, that a single bit is unique
	 * the way of definitions is 1, 2, 4, 8 ... For a short explanation:
	 * (1|2|4) -> 7, (7&1) == 1 -> true, (7&2) == 2 -> true, (7&4) == 4 -> true
	 * @see http://de.wikipedia.org/wiki/Bitweiser_Operator
	 */
	override def getIdentifier: Long = 0

	/**
	 * Returns the string identifier of the Role
	 */
	override def getRoleId: String = name

	/**
	 * return a role id which this Role will inherited from.
	 * you can return None - no parent role
	 * you can return Some(string) - one parent role
	 * you can return a List[String] more than one parent role
	 */
	override def getInheritedRoles: List[Role] = inheritedRoles

	/**
	 * returns a definition of privileges for resources.
	 *
	 * Example:
	 * return Map(
	 * 'resource-string' => List(), // allows all in this resource
	 * '^resource-string' => List(), // denies all in this resource
	 * 'resource-string' => List(
	 * 'privilege', // allow privilege
	 * '^privilege', // deny privilege
	 * 'privilege' => List('assertion-method'), // allow privilege by execute assertion \CL\Assert\Resource-String::assertion-method
	 * 'privilege' => List('assertion-method1', 'assertion-method2'), // allow privilege by execute both assertions both have to return true
	 * ), // denies all in this resource
	 * )
	 *
	 * if you define an privilege in an resource, the whole resource is denied and only defined privilegs are allowed
	 */
	override def getPrivileges: Map[Resource, Map[Privilege, Seq[Option[AclObject] => Boolean]]] = Map()
}