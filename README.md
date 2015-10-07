# Play Acl Module

This is a module to implement a simple acl system to play framework. With this
module, you can protect controller actions, or anything else.

You can protect an admin area, or hide information / actions for specific users.
It's also possible to connect a right to specific conditions for objects.

Let's say you have a shop system and want to display orders. The orders are separated
in 2 lists (open orders and closed orders). For this case you can use the acl system
to filter entries between these 2 lists.

# How does the ACL system work

The Acl system is based on classical roles. A user can have one or more roles. Each role
have resources and privileges. A resource is a entity or an are / module to separate
rights. A privilege is the action the user want to do, like read, update, delete, expand,
manage, send, ... .

A User have roles, and will be initialized with the ACL system. An Anonymous user is also
a representation of a user, but with none or different roles.

Roles are objects, with a unique identifier which. The identifier is a bit value like
1, 2, 4, 8, 16 ...

## Define resources

A resource have to extend `org.playacl.Resource` and contain a string identifier.

```
object AdminResource extends org.playacl.Resource("admin")
```

## Define Privileges

A privilege have to extend `org.playacl.Privilege` and contain a string identifier.
 
```
object ReadPrivilege extends org.playacl.Privilege("read")
```

## Define Roles

A Role have to implement `org.playacl.Role` interface, which contains 4 abstract methods:

* `getIdentifier: Long`: the role identifier bit value
* `getRoleId: String`: returns a string identifier of this role
* `getInheritedRole: List[Role]`: a list of parent roles
* `getPrivileges: Map[Resource, Map[Privilege, Seq[Option[AclObject] => Boolean]]]`: the complete definition of rights

### Assertions

Assertions, defined in `Seq[Option[AclObject] => Boolean]` are callback functions to define
specific conditions on a resource. When you have a list of users, and want to show an edit button
to directly edit this user u can use this. An admin will have no restriction, an anonymous user
is denied for this operation, but a user can edit his own entry.

```
val myId = 4
case class User(id: Int) extends AclObject
(user: Option[AclObject]) => user match {
	case u: Option[User] => u.exists(_.id == myId)
	case _ => false
}

acl.isAllowed(UserResource, EditPrivilege, Some(currentUser)) // returns true or false
```

