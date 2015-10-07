package org.playacl

/**
 * this is the abstract role
 */
abstract class Role {

	/**
	 * defines the unique "bit"-identifier for this role. Keep in mind, that a single bit is unique
	 * the way of definitions is 1, 2, 4, 8 ... For a short explanation:
	 * (1|2|4) -> 7, (7&1) == 1 -> true, (7&2) == 2 -> true, (7&4) == 4 -> true
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
	 *    'resource-string' => List(), // allows all in this resource
	 *    '^resource-string' => List(), // denies all in this resource
	 *    'resource-string' => List(
	 *         'privilege', // allow privilege
	 *         '^privilege', // deny privilege
	 *         'privilege' => List((obj: Option[AclObject]) => { true }), // allow privilege by execute assertion function
	 *         'privilege' => List((obj: Option[AclObject]) => { true }, (obj: Option[AclObject]) => { true }), // allow privilege by execute both assertions - both have to return true
	 *     ),
	 * )
	 *
	 * if you define an privilege in an resource, the whole resource is denied and only defined privilegs are allowed
	 */
	def getPrivileges: Map[Resource, Map[Privilege, Seq[Option[AclObject] => Boolean]]]
}