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

import de.ceow.security.acl.AllowLikeHelper.{AllowObject, AllowPrivilege, AllowResource}

/**
 * Acl Component using roles and identities to check if a resource/privilege
 * is allowed for the current defined identity.
 *
 * Usage:
 * new Acl(List[de.ceow.security.acl.Role](role1, role2), user)
 * This identity and it's containing roles will be used for allowed checks
 *
 * How it works:
 * the roleRegistry is used to store all defined roles and to map the identity-roles to a role object.
 * The identity itself will store only integer values as bits (1,2,4,8 ...) which maps to the role
 * identifier.
 *
 * Example:
 * Role1.id=1, Role2.id=2 ... Role4.id=8
 * Identity:roles = 11 means List(Role1, Role2, Role8)
 *
 * While adding a identity, the mapping will check if there is a role defined, generates a new unique
 * GenericRole which inherits from all mapped Roles.
 *
 * Resource:
 * Resources have to be objects / case classes. It's more used like a type
 *
 * Privilege:
 * Privileges are also just types as resources are
 *
 * Assert:
 * An assert is a class with an apply method which receive "Option[AclObject]" as it's first parameter and
 * the current acl object as it's second argument. The return value is always a boolean.
 * This type or assertion can be used to decide on "AclObject" if the resource/privilege is allowed or not.
 *
 * Example:
 * Lets say you have a site which have a company profile and related employees (Identities).
 * Companies and Employees are in relation and you want to allow some employees to change things
 * on your company profile page. So some employees will have a flag (canEditCompany).
 * Your assert can now receive the company entity and you can check if the user is related to
 * the company and is allowed to edit.
 *
 * Roles:
 * A role defines the rule definition???? and the resources, privileges and asserts. You can reduce it to
 * just resources when you want to allow every privilege in this resource (helpful for admins).
 *
 * Mapping example:
 * val rules = Map(
 *   Resource -> Map() <- allowed all privileges
 *   Resource2 -> Map(
 *     Privilege1 -> Seq(), <- no assertions
 *     Privilege2 -> Seq((value: Option[AclObject], acl: Acl) => true|false)
 *   )
 * )
 *
 * Let's see some implementations
 *
 * Examples:
 * Acl.isAllowed(Resource, Privilege)
 * Acl.isAllowed(Resource, Privilege, Some(Foo))
 *
 */
case class Acl(roles: List[Role], user: Identity) extends AllowLike {

  /**
   * available roles
   */
  lazy val roleRegistry: List[Role] = roles

  /**
   * rule definitions
   */
  lazy val rules: Map[String, Seq[Assert]] = applyRules(roles)

  /**
   * the role of the observer itself
   */
  lazy val observerRole: Role = generateGenericRole(user)

  /**
   * the observer (Identity)
   */
  def observerEntity: Identity = user

  /**
   * create the custom role for the current user
   */
  protected def generateGenericRole(user: Identity): Role = {

    val inheritedRoles = roleRegistry.filter(role => (user.roles & role.getIdentifier) == role.getIdentifier)
    val cleaned = inheritedRoles.foldLeft(List[Role]()) { (clean,role) =>

      if (inheritedRoles.exists(_.contains(role))) {
        clean
      } else {
        role :: clean
      }
    }

    GenericRole("user_role_%d" format user.id, cleaned)
  }

  /**
   * apply the rule definition from a role to a easy understandable format for the acl to
   * check against
   */
  protected def applyRules(roles: List[Role]): Map[String, Seq[Assert]] = {

    val convertedRules = for {
      role ← roles
      rules ← role.getPrivileges
      roleRules ← rules._2
    } yield createHash(role, rules._1, roleRules._1) → roleRules._2

    convertedRules.toMap
  }

  /**
   * creates a hash value by give role, resource and privilege
   * RoleName/ResourceName/PrivilegeName
   * RoleName/ResourceName/\* for allowing all privileges in this resource
   */
  protected def createHash(role: Role, r: Resource, p: Privilege): String = {

    s"${role.getRoleId}/${r.name}/${p.name}"
  }

  /**
   * check methods if a resource and a privilege is allowed by itself or with object
   */
  def isAllowed(r: Resource, p: Privilege, o: Option[AclObject] = None): Boolean = allowed(observerRole, r, p, o)

  /**
   * internal, recursive check method.
   * 1. check if a resource is allowed including all privileges
   * 2. check if a resource is allowed for given privilege
   * 3. check all inheritance roles by using recursion
   */
  protected def allowed(role: Role, resource: Resource, privilege: Privilege, objectToCheck: Option[AclObject]): Boolean = {

    def checkForRole: Boolean = {

      val hash = createHash(role, resource, privilege)

      def assertions: List[Boolean] = rules.collect {

        case (`hash`, definition) ⇒ definition.map(_ apply(objectToCheck, this))
      }.toList.flatten

      (rules contains hash) && !(assertions contains false)
    }

    def denied: Boolean = {

      val deniedHash = createHash(role, resource, privilege.deny())
      rules contains deniedHash
    }

    def inheritedRoles = role.getInheritedRoles.map(allowed(_, resource, privilege, objectToCheck))

    !denied && (checkForRole || (inheritedRoles contains true))
  }

  /**
   * allow like for acl instance to check for resource
   *
   * @param r   the resource
   * @param acl the acl instance
   * @return
   */
  override def allows(r: Resource)(implicit acl: Acl): AllowPrivilege = new AllowLikeHelper.AllowPrivilege(r, None)

  /**
   * allow like for privilege to check for
   *
   * @param p   the privilege
   * @param acl the acl instance
   * @return
   */
  override def allows(p: Privilege)(implicit acl: Acl): AllowResource = new AllowLikeHelper.AllowResource(p, None)

  /**
   * allows for acl object
   *
   * @param v   AclObject
   * @param acl the acl instance
   * @return
   */
  def allows(v: AclObject)(implicit acl: Acl): AllowObject = new AllowLikeHelper.AllowObject(Some(v))
}

