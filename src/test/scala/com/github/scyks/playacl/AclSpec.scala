package com.github.scyks.playacl

import TestDefinitions._
import org.specs2.mutable._

class AclSpec extends Specification {

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

			val user = new User
			user.roles = Guest.getIdentifier | Admin.getIdentifier
			val acl = new Acl(List(Guest, Admin), user)

			acl.isAllowed(AdminResource, ManagePrivilege) must beTrue
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
