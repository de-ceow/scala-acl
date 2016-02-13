package com.github.scyks.playacl

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
		override def getPrivileges: Map[Resource, Map[Privilege, Seq[(Option[AclObject], Acl) => Boolean]]] = {
			Map(
				MainResource -> Map(
					ReadPrivilege -> Seq()
				),
				UserResource -> Map(
					ReadPrivilege -> Seq(),
					CreatePrivilege -> Seq((obj: Option[AclObject], acl: Acl) => {
						obj match {
							case Some(u: User) => u.id == 3
							case _ => false
						}
					})
				)
			)
		}
		override def getInheritedRoles: List[Role] = List()
		override def getRoleId: String = "guest"
	}
	object Registered extends Role {

		override def getIdentifier: Long = 2L
		override def getPrivileges: Map[Resource, Map[Privilege, Seq[(Option[AclObject], Acl) => Boolean]]] = {
			Map(
				UserResource -> Map(
					LoggedInPrivilege -> Seq()
				)
			)
		}
		override def getInheritedRoles: List[Role] = List(Guest)
		override def getRoleId: String = "registered"
	}

	object Admin extends Role {

		override def getIdentifier: Long = 4L
		override def getPrivileges: Map[Resource, Map[Privilege, Seq[(Option[AclObject], Acl) => Boolean]]] = {
			Map(
				AdminResource -> Map(
					ReadPrivilege -> Seq(),
					ManagePrivilege -> Seq()
				)
			)
		}
		override def getInheritedRoles: List[Role] = List(Registered)
		override def getRoleId: String = "admin"
	}

	class WithAllowLike extends AllowLike

	case class User(id: Long = 0L) extends Identity with AclObject {

		var roles: Long = 3L

	}

	object ObjectToCheck extends AclObject
}
