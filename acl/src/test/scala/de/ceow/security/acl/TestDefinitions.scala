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
 * Test definition object to define a implementation of
 * the ACL system for an integration test
 */
object TestDefinitions {

  /** Resources */
  object MainResource extends Resource("main")
  object UserResource extends Resource("user")
  object AdminResource extends Resource("admin")

  /** Privileges */
  object ReadPrivilege extends Privilege("read")
  object CreatePrivilege extends Privilege("create")
  object LoggedInPrivilege extends Privilege("loggedIn")
  object ManagePrivilege extends Privilege("manage")

  /** User Roles */
  object Guest extends Role {

    override def getIdentifier: Long = 1L

    override def getPrivileges: Map[Resource, Map[Privilege, Seq[Assert]]] = {
      Map(
        MainResource → Map(
          ReadPrivilege → Seq()
        ),
        UserResource → Map(
          ReadPrivilege → Seq(),
          CreatePrivilege → Seq(Asserts.UserIs3Assert)
        )
      )
    }

    override def getInheritedRoles: List[Role] = List()

    override def getRoleId: String = "guest"
  }

  object Registered extends Role {

    override def getIdentifier: Long = 2L

    override def getPrivileges: Map[Resource, Map[Privilege, Seq[Assert]]] = {
      Map(
        UserResource → Map(
          ReadPrivilege → Seq(Asserts.UserIsMeAssert),
          LoggedInPrivilege → Seq()
        ),
        MainResource → Map(
          ReadPrivilege.deny() → Seq()
        )
      )
    }

    override def getInheritedRoles: List[Role] = List(Guest)

    override def getRoleId: String = "registered"
  }

  object Admin extends Role {

    override def getIdentifier: Long = 4L

    override def getPrivileges: Map[Resource, Map[Privilege, Seq[Assert]]] = {
      Map(
        AdminResource → Map(
          ReadPrivilege → Seq(),
          ManagePrivilege → Seq()
        )
      )
    }

    override def getInheritedRoles: List[Role] = List(Registered)

    override def getRoleId: String = "admin"
  }

  class WithAllowLike extends AllowLike

  case class User(id: Long = 1, name: String = "user", roles: Long = 3L) extends Identity with AclObject

  object ObjectToCheck extends AclObject

  object Asserts {

    object UserIs3Assert extends Assert {

      override def apply(obj: Option[AclObject], acl: Acl): Boolean = obj match {
        case Some(u: User) ⇒ u.id == 3
        case _ ⇒ false
      }
    }

    object UserIsMeAssert extends Assert {

      override def apply(obj: Option[AclObject], acl: Acl): Boolean = obj match {
        case Some(u: User) ⇒ u.id == acl.observerEntity.id
        case _ ⇒ false
      }
    }
  }
}
