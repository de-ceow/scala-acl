package org.playacl

/**
 * Allow like trait
 */
trait AllowLike {

//	/**
//	 * Allow stuff
//	 */
//	class AllowPrivilege(r: Resource, objectToCheck: Option[AclObject]) {
//
//		def to(p: Privilege): Boolean = Acl.isAllowed(r, p, objectToCheck)
//	}
//
//	def allows(r: Resource) = {
//
//		this match {
//			case a: Acl => new AllowPrivilege(r, None)
//			case b: AclObject => new AllowPrivilege(r, Some(b))
//			case _ => new AllowPrivilege(r, None)
//		}
//	}
//
//	def allows(p: Privilege) = {
//
//		this match {
//			case r: Resource => new AllowPrivilege(r, None) to p
//			case _ => throw new Exception("AllowLike is not able to handle the input")
//		}
//	}
//
//	def allows(v: Any) = {
//
//		this match {
//			case r: Resource => new AllowPrivilege(r, None)
//			case _ => throw new Exception("AllowLike is not able to handle the input")
//		}
//	}
}