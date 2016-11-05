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

import de.ceow.security.acl.TestDefinitions._
import org.specs2.mutable._

class AclSpec extends Specification {

  "Roles.contains" should {

    "Guest does not contain Admin" in {

      Guest contains Admin must beFalse
    }

    "Registered does not contain Admin" in {

      Registered contains Admin must beFalse
    }

    "Registered contains Guest" in {

      Registered contains Guest must beTrue
    }

    "Admin contains Registered" in {

      Admin contains Registered must beTrue
    }

    "Admin contains Guest" in {

      Admin contains Guest must beTrue
    }
  }
	"Acl component" should {

		"be able to add roles" in {

			val acl = new Acl(List(Guest), new User)
			acl.roleRegistry.contains(Guest) must beTrue
		}

		"be able to check if a role is defined" in {

			val acl = new Acl(List(Guest), new User)
			acl.roleRegistry.contains(Guest) must beTrue
			acl.roleRegistry.contains(Admin) must beFalse
		}

		"add resources on adding roles" in {

			"for guest" in {

				val acl = new Acl(List(Guest), new User)
				acl.rules.contains("guest/main/read") must beTrue
				acl.rules.contains("guest/user/read") must beTrue
				acl.rules.contains("guest/user/create") must beTrue
			}

			"for registered" in {

				val acl = new Acl(List(Registered), new User)
				acl.rules.contains("registered/user/loggedIn") must beTrue
			}

			"for admin" in {

				val acl = new Acl(List(Admin), new User)
				acl.rules.contains("admin/admin/read") must beTrue
				acl.rules.contains("admin/admin/manage") must beTrue
			}
		}

		"be able to allow guest for main/read and" in {

			val acl = new Acl(List(Guest), new User)
			acl.isAllowed(MainResource, ReadPrivilege) must beTrue
		}

		"not be able to allow guest for user/isLoggedIn because it's denies" in {

			val acl = new Acl(List(Guest), new User)
			acl.isAllowed(UserResource, LoggedInPrivilege) must beFalse
		}

		"is be able to allow Admin for admin/manage" in {

			val user = new User(roles = Guest.getIdentifier | Admin.getIdentifier)
			val acl = new Acl(List(Guest, Admin), user)

			acl.isAllowed(AdminResource, ManagePrivilege) must beTrue
		}

		"explicitly denies a privilege" in {

			val user = new User(roles = Guest.getIdentifier | Registered.getIdentifier)
			val acl = new Acl(List(Guest, Registered), user)

			acl.isAllowed(MainResource, ReadPrivilege) must beFalse
		}
	}

	"Allow Like" in {

		"enables Acl to allow resources" in {

			implicit val acl: Acl = new Acl(List(Guest), new User)
			acl allows MainResource to ReadPrivilege must beTrue
		}

		"enables Acl to allow privilege" in {

			implicit val acl: Acl = new Acl(List(Guest), new User)
			acl allows ReadPrivilege at MainResource must beTrue
		}

		"enables Acl to allow aclObject" in {

			implicit val acl: Acl = new Acl(List(Guest), new User)
			val user = new User(3)
			acl allows user at UserResource to CreatePrivilege must beTrue
		}

		"enables Acl to allow aclObjet - denied" in {

			implicit val acl: Acl = new Acl(List(Guest), new User)
			val user = new User
			acl allows user at UserResource to CreatePrivilege must beFalse
		}

		"enables AclObject to allow resources" in {

			implicit val acl: Acl = new Acl(List(Guest), new User)

			val classWithAllowLike = new WithAllowLike()
			classWithAllowLike allows MainResource to ReadPrivilege must beTrue
		}

		"enables AclObject to allow privilege" in {

			implicit val acl: Acl = new Acl(List(Guest), new User)

			val classWithAllowLike = new WithAllowLike()
			classWithAllowLike allows ReadPrivilege at MainResource must beTrue
		}

		"enables Resource to allow privilege" in {

			implicit val acl: Acl = new Acl(List(Guest), new User)
			MainResource allows ReadPrivilege must beTrue
		}

		"enables Resource to allow AclObject" in {

			implicit val acl: Acl = new Acl(List(Guest), new User)

			val user = new User(3)
			UserResource allows user to CreatePrivilege must beTrue
		}

		"enables Resource to allow AclObject - denied" in {

			implicit val acl: Acl = new Acl(List(Guest), new User)

			val user = new User
			UserResource allows user to CreatePrivilege must beFalse
		}

		"enables Privilege to allow Resource" in {

			implicit val acl: Acl = new Acl(List(Guest), new User)

			ReadPrivilege allows MainResource  must beTrue
		}

		"enables Privilege to allow AclObject" in {

			implicit val acl: Acl = new Acl(List(Guest), new User)

			val user = new User(3)
			CreatePrivilege allows user at UserResource must beTrue
		}

		"enables Privilege to allow AclObject - denied" in {

			implicit val acl: Acl = new Acl(List(Guest), new User)

			val user = new User
			CreatePrivilege allows user at UserResource must beFalse
		}
	}
}
