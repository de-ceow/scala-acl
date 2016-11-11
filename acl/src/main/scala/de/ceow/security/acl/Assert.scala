package de.ceow.security.acl

/**
 * This is a abstract class to writer assertions for some rights.
 *
 * {{{
 * class myAssert extends Assert {
 *   def apply(obj: Option[AclObject], acl: Acl): Boolean = {
 *     obj.match {
 *       case Some(item) => true
 *       case None => false
 *     }
 *   }
 * }
 * }}}
 */
abstract class Assert {

  /**
   * Check method for the assert.
   *
   * @param obj the given aclObject
   * @param acl the current aclInstance
   * @return
   */
  def apply(obj: Option[AclObject], acl: Acl): Boolean
}
