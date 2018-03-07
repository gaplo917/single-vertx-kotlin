package io.vertx.example

interface UserRepository {
  fun findUsers(): List<User>
}

class UserRepositoryImpl(): UserRepository {
  override fun findUsers(): List<User> {
    return listOf(User(1,"gary"), User(2, "george"))
  }
}
