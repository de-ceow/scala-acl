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