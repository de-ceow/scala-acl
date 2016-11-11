package de.ceow.security.play_acl

import de.ceow.security.acl._
import play.api.mvc.{Controller, RequestHeader, Results}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object TestDefinition {

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

    override def getPrivileges: Map[Resource, Map[Privilege, Seq[Assert]]] = {
      Map(
        MainResource → Map(
          ReadPrivilege → Seq()
        ),
        UserResource → Map(
          ReadPrivilege → Seq(),
          CreatePrivilege → Seq(Asserts.UserIs3Assert)
        )
      )
    }

    override def getInheritedRoles: List[Role] = List()

    override def getRoleId: String = "guest"
  }

  object Registered extends Role {

    override def getIdentifier: Long = 2L

    override def getPrivileges: Map[Resource, Map[Privilege, Seq[Assert]]] = {
      Map(
        UserResource → Map(
          ReadPrivilege → Seq(Asserts.UserIsMeAssert),
          LoggedInPrivilege → Seq()
        ),
        MainResource → Map(
          ReadPrivilege.deny() → Seq()
        )
      )
    }

    override def getInheritedRoles: List[Role] = List(Guest)

    override def getRoleId: String = "registered"
  }

  object Admin extends Role {

    override def getIdentifier: Long = 4L

    override def getPrivileges: Map[Resource, Map[Privilege, Seq[Assert]]] = {
      Map(
        AdminResource → Map(
          ReadPrivilege → Seq(),
          ManagePrivilege → Seq()
        )
      )
    }

    override def getInheritedRoles: List[Role] = List(Registered)

    override def getRoleId: String = "admin"
  }

  case class User(id: Long = 1, name: String = "user", roles: Long = 3L) extends Identity with AclObject

  class WithAllowLike extends AllowLike

  object ObjectToCheck extends AclObject

  class UnauthorizedException extends Exception
  class UnauthenticatedException extends Exception

  trait MySecurity extends AclSecurity[User] with Results {

    override def userByUsername(username: String)(implicit request: RequestHeader): Option[User] = username match {

      case "user" ⇒ Some(new User(1, "user", 3L))
      case "admin" ⇒ Some(new User(2, "admin", 7L))
      case _ ⇒ None
    }
    override def guestRole: Role = Guest
    override def guestUser: User = new User(1, "guest", 1L)
    override def roles: List[Role] = List(Guest, Registered, Admin)

    override def onUnauthenticated(request: RequestHeader) = BadRequest("Unauthenticated")
    override def onUnauthorized(request: RequestHeader) = BadRequest("Unauthorized")
  }

  object Asserts {

    object UserIs3Assert extends Assert {

      override def apply(obj: Option[AclObject], acl: Acl): Boolean = obj match {
        case Some(u: User) ⇒ u.id == 3
        case _ ⇒ false
      }
    }

    object UserIsMeAssert extends Assert {

      override def apply(obj: Option[AclObject], acl: Acl): Boolean = obj match {
        case Some(u: User) ⇒ u.id == acl.observerEntity.id
        case _ ⇒ false
      }
    }
  }

  trait MyAsyncSecurity extends AsyncAclSecurity[User] with MySecurity {

  }

  class ExampleController extends Controller with MySecurity with MyAsyncSecurity {

    def withAuthAction = withAuth { username ⇒  implicit request ⇒
      Ok("OK " + username)
    }

    def withUserAction = withUser { user: User ⇒ implicit request ⇒
      Ok("OK " + user.name)
    }

    def withAclAction = withAcl { implicit acl: Acl ⇒ implicit request ⇒
      Ok("OK")
    }

    def withProtectedAction = withProtected(AdminResource, ReadPrivilege) { implicit request ⇒
      Ok("OK")
    }

    def withProtectedResourceAction(id: Long) = withProtected(AdminResource, ReadPrivilege, () ⇒ Some(new User(id = id))) {user: Option[User] ⇒ implicit request ⇒
      Ok("OK " + user.map(_.name).getOrElse("unknown"))
    }

    def withProtectedAclAction = withProtectedAcl(AdminResource, ReadPrivilege) { implicit acl: Acl ⇒ implicit request ⇒
      Ok("OK")
    }

    def withProtectedAclResourceAction(id: Long) = withProtectedAcl(AdminResource, ReadPrivilege, () ⇒ Some(new User(id = id))) { user: Option[User] ⇒ acl: Acl ⇒ implicit request ⇒
      Ok("OK " + user.map(_.name).getOrElse("unknown"))
    }

    def withAuthActionAsync = withAuthAsync { username ⇒  implicit request ⇒
      Future(Ok("OK " + username))
    }

    def withUserActionAsync = withUserAsync { user: User ⇒ implicit request ⇒
      Future(Ok("OK " + user.name))
    }

    def withAclActionAsync = withAclAsync { implicit acl: Acl ⇒ implicit request ⇒
      Future(Ok("OK"))
    }

    def withProtectedActionAsync = withProtectedAsync(AdminResource, ReadPrivilege) { implicit request ⇒
      Future(Ok("OK"))
    }

    def withProtectedResourceActionAsync(id: Long) = withProtectedAsync(AdminResource, ReadPrivilege, () ⇒ Some(new User(id = id))) {user: Option[User] ⇒ implicit request ⇒
      Future(Ok("OK " + user.map(_.name).getOrElse("unknown")))
    }

    def withProtectedAclActionAsync = withProtectedAclAsync(AdminResource, ReadPrivilege) { implicit acl: Acl ⇒ implicit request ⇒
      Future(Ok("OK"))
    }

    def withProtectedAclResourceActionAsync(id: Long) = withProtectedAclAsync(AdminResource, ReadPrivilege, () ⇒ Some(new User(id))) { user: Option[User] ⇒ acl: Acl ⇒ implicit request ⇒
      Future(Ok("OK " + user.map(_.name).getOrElse("unknown")))
    }
  }
}
