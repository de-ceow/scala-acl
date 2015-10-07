package org.playacl

/**
 * Test definition object to define a implementation of
 * the ACL system for an integration test
 */
object TestDefinitions {

	object Competition extends Resource("competition")
	object Show extends Privilege("show")
	object Expand extends Privilege("expand")
	object Manage extends Privilege("manage")
	object Edit extends Privilege("edit")

	object Guest extends Role {
		override def getIdentifier: Long = 1L
		override def getRoleId: String = "guest"
		override def getPrivileges: Map[Resource, Map[Privilege, Seq[Option[AclObject] => Boolean]]] = Map(
			Competition -> Map(
				Show -> Seq((value: Option[AclObject]) => true),
				Expand -> Seq(),
				Edit -> Seq((value: Option[AclObject]) => false)
			)
		)
		override def getInheritedRoles = List()
	}


	object Admin extends Role {
		override def getIdentifier: Long = 2L
		override def getRoleId: String = "admin"
		override def getPrivileges: Map[Resource, Map[Privilege, Seq[Option[AclObject] => Boolean]]] = Map(
			Competition -> Map(
				Manage -> Seq()
			)
		)
		override def getInheritedRoles = List(Guest)
	}

	class WithAllowLike extends AllowLike

	class User extends Identity {

		var roles: Long = 3L
		val id: Long = 0L
	}

	object ObjectToCheck extends AclObject

}
