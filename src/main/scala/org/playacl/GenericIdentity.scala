package org.playacl

/**
 * This is a generic implementation of Identity trait and a fake Identity itself
 */
case class GenericIdentity(name: String, givenRoles: Long = 0L) extends Identity {

	var roles: Long = givenRoles
	val id: Long = 0L
}

