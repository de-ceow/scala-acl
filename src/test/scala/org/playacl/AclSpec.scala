package org.playacl

import TestDefinitions._
import org.specs2.mutable._

class AclSpec extends Specification {

	"Acl component" should {
		"be able to add roles" in {

			val Acl = new Acl[User](List(Guest), new User)
			Acl.roleRegistry.contains(Guest) must beTrue
		}

		"be able to check if a role is defined" in {

			val Acl = new Acl[User](List(Guest), new User)
			Acl.roleRegistry.contains(Guest) must beTrue
			Acl.roleRegistry.contains(Admin) must beFalse
		}

		"add resources on adding roles" in {

			val Acl = new Acl[User](List(Guest), new User)
			Acl.rules.contains("guest/competition/show") must beTrue
			Acl.rules.contains("guest/competition/expand") must beTrue
		}

		"be able to allow guest for competition/show and competition/expand" in {

			val Acl = new Acl[User](List(Guest), new User)
			Acl.isAllowed(Competition, Show, Some(ObjectToCheck)) must beTrue
			Acl.isAllowed(Competition, Expand) must beTrue
		}

		"not be able to allow guest for competition/manage because it's denies" in {

			val Acl = new Acl[User](List(Guest), new User)
			Acl.isAllowed(Competition, Manage) must beFalse
		}

		"is be able to allow Admin for competition/manage" in {

			val user = new User
			user.roles = Guest.getIdentifier | Admin.getIdentifier
			val Acl = new Acl[User](List(Guest, Admin), user)

			Acl.isAllowed(Competition, Manage) must beTrue
			Acl.isAllowed(Competition, Expand) must beTrue
		}

		"be able to handle AllowLike style of assertions" in {

			val Acl = new Acl[User](List(Guest), new User)

			val classWithAllowLike = new WithAllowLike()

			Acl.isAllowed(Competition, Edit) must beFalse
		}
	}
}
