# Play Acl Module

*A lightweight and flexible ACL component*

[![Build Status](https://travis-ci.org/de-ceow/scala-acl.svg?branch=master)](https://travis-ci.org/Scyks/play-acl)


This is a module to implement a simple ACL system to any of your applications. 
By using the play plugin you can protect controller actions, or anything else.

You can protect an admin area, or hide information / actions for specific users.
It's also possible to connect a right to specific conditions for objects.

Let's say you have a shop system and want to display orders. The orders are separated
in 2 lists (open orders and closed orders). For this case you can use the acl system
to filter entries between these 2 lists.

# Install

You can easily use the sbt tool to download the resources to your project.

```scala
libraryDependencies  ++=  Seq(
	"de.ceow" %% "scala-acl" % "1.2.0",
	"de.ceow" %% "play-acl" % "1.2.0" // if you run play
)
```

# How does the ACL system work

The Acl system is based on classic roles. A user can have one or more roles. Each role
have resources and privileges. A resource is a entity or an are / module to separate
rights. A privilege is the action the user want to do, like read, update, delete, expand,
manage, send, ... .

A User have roles, and will be initialized with the ACL system. An Anonymous user is also
a representation of a user, but with none or different roles.

Roles are objects, with a unique identifier which. The identifier is a bit value like
1, 2, 4, 8, 16 ...

## Define resources

A resource have to extend `de.ceow.security.acl.Resource` and contain a string identifier.

```scala
object AdminResource extends de.ceow.security.acl.Resource("admin")
```

## Define Privileges

A privilege have to extend `de.ceow.security.acl.Privilege` and contain a string identifier.
 
```scala
object ReadPrivilege extends de.ceow.security.acl.Privilege("read")
```

## Define Roles

A Role have to implement `de.ceow.security.acl.Role` interface, which contains 4 abstract methods:

* `getIdentifier: Long`: the role identifier bit value
* `getRoleId: String`: returns a string identifier of this role
* `getInheritedRole: List[Role]`: a list of parent roles
* `getPrivileges: Map[Resource, Map[Privilege, Seq[Assert]]]`: the complete definition of rights

### Assertions

Assertions are classes with an apply method. The parameters are`Option[AclObject]` and `Acl`.
You can check specific conditions on a resource. When you have a list of users, and want to show an edit button
to directly edit this user `u` can use this. An admin will have no restriction, an anonymous user
is denied for this operation, but a user can edit his own entry.

```scala
object myRole extends Role {
  override def getPrivileges: Map[Resource, Map[Privilege, Seq[Assert]]] = Map(
    Resource -> Map(
      Privilege -> Seq(Asserts.isMe)
    )
  }
}
val myId = 4
case class User(id: Int) extends AclObject
object Asserts {

  object IsMe extends Assert {
    // the assert definition
    def apply(obj: Option[AclObject], acl: Acl) => obj match {
      case Some(u: User) => u.exists(_.id == acl.observerEntity.id)
      case _ => false
    }
  }
}

acl.isAllowed(UserResource, EditPrivilege, Some(currentUser)) // returns true or false
```
# Play Framework

## The Security Trait

For Initialization of the Acl you need to configure the Security Trait. This Trait will be used
in a controller action to initialize and retrieve the ACL instance for the current user. When there
is no user logged in, there is a guest user defined. To implement the Security trait you need to
define these methods:

* `userByUsername(username: String)(implicit acl: Acl): Option[I]` to retrieve a user from some storage by given username
* `roles: List[Role]` the list of available roles
* `guestRole: Role` the guest role
* `guestUser: UserEntity` to return a guest / anonymous user

## Putting all Together

To configure your acl system you have to define "Resources", "Privileges", "Roles" and the "Security" trait.
I'll show a dummy implementation:

```scala
import de.ceow.security.acl.{Resource, Privilege, Role, Identity}
import de.ceow.security.acl.play.Security

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
			MainResource -> Map(
				ReadPrivilege -> Seq()
			),
			UserResource -> Map(
				ReadPrivilege -> Seq(),
				CreatePrivilege -> Seq()
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
	override def getPrivileges: Map[Resource, Map[Privilege, Seq[Assert]]] = {
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

case class UserEntity(id: Int, roles: Long) extends Identity

trait Security extends de.ceow.security.acl.play.AclSecurity[UserEntity] {
	def userByUsername(username: String)(implicit request: RequestHeader): Option[UserEntity] = {
		UserRepository.findByUserName(username) match {
			case Success(user) => Some(user)
			case Failure(ex) => None
		}
	}
	override def roles: List[Role] = Guest :: Registered :: Admin:: Nil
	override def guestRole: Role = Guest
	override def guestUser: UserEntity = new UserEntity(0, 1L)
	override def onUnauthenticated(request: RequestHeader) = Results.Redirect("/login")
	override def onUnauthorized(request: RequestHeader) = Results.Redirect("/login")
}
trait AsyncSecurity extends de.ceow.security.acl.play.AsyncSecurity[UserEntity] with Security {}
```

# Protecting Controller instances or get ACL instance

Because of being stateless, the controller is the entry point for the ACL system and 
it will be initialized there. Also when you want to retrieve the logged in user.

The Standard implementation contains 4 Methods, which are checking if a user is logged in
and return the Acl or the user instance or even check a resource and privilege directly.

**Normal Security Trait**

* `withAuth`: check if user is logged in, otherwise `onUnauthenticated` will be called
* `withUser`: will provide the logged in or the guest user instance
* `withAcl`: will provide the acl instance
* `withProtected(r: Resource, p: Privilege)`: 
    * if acl check against resource privilege fails -> `onUnauthorized` will be called
* `withProtected(r: Resource, p: Privilege, objectToCheck: () => Option[AclObject])`: 
    * if acl check against resource privilege on objectToCheck fails -> `onUnauthorized` will be called
* `withProtected(r: Resource, p: Privilege, objectToCheck: Acl => Option[AclObject])`: 
    * same as above except the Acl as an input parameter
* `withProtectedAcl(r: Resource, p: Privilege)`: 
    * if acl check against resource privilege fails -> `onUnauthorized` will be called
    * if logged in and acl check is true -> return Acl Instance
* `withProtectedAcl(r: Resource, p: Privilege, objectToCheck: () => Option[AclObject])`: 
    * if acl check against resource privilege on objectToCheck fails -> `onUnauthorized` will be called
    * if logged in and acl check is true -> return Acl Instance
* `withProtectedAcl(r: Resource, p: Privilege, objectToCheck: Acl => Option[AclObject])`: 
    * same as above except the Acl as an input parameter
 
**Async Security Trait**

* `withAuthAsync`: check if user is logged in, otherwise `onUnauthenticated` will be called
* `withUserAsync`: will provide the logged in or the guest user instance
* `withAclAsync`: will provide the acl instance
* `withProtectedAsync(r: Resource, p: Privilege)`: 
    * if acl check against resource privilege fails -> `onUnauthorized` will be called
* `withProtectedAsync(r: Resource, p: Privilege, objectToCheck: () => Option[AclObject])`: 
    * if acl check against resource privilege on objectToCheck fails -> `onUnauthorized` will be called
* `withProtectedAsync(r: Resource, p: Privilege, objectToCheck: Acl => Option[AclObject])`: 
    * same as above except the Acl as an input parameter
* `withProtectedAclAsync(r: Resource, p: Privilege)`: 
    * if acl check against resource privilege fails -> `onUnauthorized` will be called
    * if logged in and acl check is true -> return Acl Instance
* `withProtectedAclAsync(r: Resource, p: Privilege, objectToCheck: () => Option[AclObject])`: 
    * if acl check against resource privilege on objectToCheck fails -> `onUnauthorized` will be called
    * if logged in and acl check is true -> return Acl Instance
* `withProtectedAclAsync(r: Resource, p: Privilege, objectToCheck: Acl => Option[AclObject])`: 
    * same as above except the Acl as an input parameter


# Implementation in controller

To protect a controller action or retrieve Acl or current user, see the following example:

```scala
package controllers

import <your security trait>
import <your resources and privileges>
import <your user entity>
import play.api.mvc.InjectedController
import de.ceow.security.acl.Acl

/** Admin controller - we want to protected this */
class Admin @Inject()(val messagesApi: MessagesApi, override val configuration: Configuration) extends InjectedController with Security {

	def dashboard = withAuth { implicit request =>
		Ok("")
	}
	
	def dashboard = withUser { implicit user: UserEntity => implicit request =>
		Ok("")
	}
	
	def dashboard = withAcl { implicit acl: Acl => implicit request =>
		Ok("")
	}
	
	def dashboard = withProtected(AdminResource, ReadPrivilege) { implicit request =>
    	Ok("")
	}
	
	def dashboard = withProtected(AdminResource, ReadPrivilege, DataSource.getData) { user => implicit request =>
    	Ok("")
	}

	def dashboard = withProtectedAcl(AdminResource, ReadPrivilege) { implicit acl: Acl => implicit request =>
    	Ok("")
	}
	
	def dashboard = withProtectedAcl(AdminResource, ReadPrivilege, DataSource.getData) { user => implicit acl: Acl => implicit request =>
    	Ok("")
	}
}
```

# Using acl protection in views / controller

A Requirement for doing this is an instance of `acl: Acl` in the current context. The acl object
will be passed as `implicit` parameter.

For checking the rights you have a lot of options to ask:

* `acl.isAllowed(Resource, Privilege, Some(AclObject))`

__use `AllowLike` trait__

To use this `AllowLike` trait, you need an AclObject.

`case class User extends AclObject With AllowLike`

* `acl allows Resource to Privilege`
* `acl allows Privilege at Resource`
* `acl allows AclObject ad Resource to Privilege`
* `AclObject allows Resource to Privilege`
* `AclObject allows Privilege at Resource`
* `Resource allows Privilege`
* `Resource allows AclObject to Privilege`
* `Privilege allows Resource`
* `Privilege allows AclObject at Resource`

## View implementation

In a view it's necessary to provide implicit acl instance.

__without request__

```scala
@import de.ceow.security.acl.Acl
@()(implicit acl: Acl)

@if(acl allows Resource to Privilege) {
	<h1>Hello World</h1>
}
```

__with request__

```scala
@import de.ceow.security.acl.Acl
@()(implicit request: RequestHeader, acl: Acl)

@if(acl allows Resource to Privilege) {
	<h1>Hello World</h1>
}
```
## Changelog

### 1.2.0
*scala-acl*
- add Scala version (2.11.11, 2.12.1, 2.12.2)
- simplify pattern matching

*play-acl* 
- removed `new` for case class creation
- allow to provide acl object as an input for `objectToCheck`
  beside `withProtectedAcl(r: Resource, p: Privilege, objectToCheck: () => Option[AclObject])`
  also `withProtectedAcl(r: Resource, p: Privilege, objectToCheck: Acl => Option[AclObject])`

### 1.1.0

- Change Assert from function to abstract class

### 1.0.0

- separate acl and play-acl to different modules
- implement Privilege.deny rule
- change inheritance to use role inheritance only
- use Acl.Assert as type definition for assertions
- update play framework to 2.5.x

### 0.7.0

- implement async trait to use acl also in async actions

### 0.6.0

- implementing unit tests for Security Trait
- reuse code blocks in security trait
- pass checked object to controller action
- implement travis ci

### 0.5.0

- update scala version to 2.11.8
- update play framework to 2.5.0

### 0.4.0

- passing the acl instance to the assertion
- implement 2 new methods to check also in play controller against an AclObject

