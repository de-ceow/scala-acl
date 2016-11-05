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
package de.ceow.security.acl

/**
 * this is the abstract role
 */
abstract class Role {

  /**
   * defines the unique "bit"-identifier for this role. Keep in mind, that a single bit is unique
   * the way of definitions is 1, 2, 4, 8 ... For a short explanation:
   * (1|2|4) -> 7, (7&1) == 1 -> true, (7&2) == 2 -> true, (7&4) == 4 -> true
   *
   * @see http://de.wikipedia.org/wiki/Bitweiser_Operator
   */
  def getIdentifier: Long

  /**
   * Returns the string identifier of the Role
   */
  def getRoleId: String

  /**
   * return a list of roles which this Role will inherited from
   */
  def getInheritedRoles: List[Role]

  /**
   * returns a definition of privileges for resources.
   *
   * Example:
   * return Map(
   *   de.ceow.security.acl.Resource → Seq(), // allows all in this resource
   *   de.ceow.security.acl.Resource → Seq(
   *     de.ceow.security.acl.Privilege, // allow privilege
   *     // allow privilege by execute assertion function
   *     de.ceow.security.acl.Privilege → Seq((obj: Option[AclObject], acl: Acl) ⇒ true),
   *     // allow privilege by execute both assertions - both have to return true
   *     de.ceow.security.acl.Privilege → Seq((obj: Option[AclObject], acl: Acl) ⇒ true, (obj: Option[AclObject], acl: Acl) ⇒ true)
   *   ),
   * )
   *
   * if you define an privilege in an resource, the whole resource is denied and only defined privileges are allowed
   */
  def getPrivileges: Map[Resource, Map[Privilege, Seq[Acl.Assert]]]

  /**
   * check if this or inherited roles contain a given role
   * @param role the role to check
   * @return
   */
  private[acl] def contains(role: Role): Boolean = getInheritedRoles.exists(r ⇒ r == role || r.contains(role))
}
