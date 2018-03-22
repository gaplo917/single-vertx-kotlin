package io.vertx.example.services

import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.lazy
import io.vertx.example.Injection
import io.vertx.example.jooq.tables.pojos.VertxUser
import io.vertx.example.repositories.UserRepository
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.jooq.DSLContext
import kotlin.coroutines.experimental.suspendCoroutine

interface UserService {
    fun findUserByIdSync(id: Int): VertxUser?
    suspend fun findUserById(id: Int): VertxUser?
}

class UserServiceImpl : UserService {
    private val userRepository: UserRepository by Injection.lazy.instance()

    override fun findUserByIdSync(id: Int): VertxUser? {
        return userRepository.findUserById(id)
    }

    override suspend fun findUserById(id: Int): VertxUser? {
        return async { userRepository.findUserById(id) }.await()
    }

}
