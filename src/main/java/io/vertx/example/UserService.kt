package io.vertx.example

data class User(
  val id: Long,
  val name: String
)

interface UserService {
  fun findUserById(id: Long): User
}

class UserServiceImpl: UserService {
  override fun findUserById(id: Long): User {
    return User(id = id, name = "someone-$id")
  }
}
