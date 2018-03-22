package io.vertx.example.repositories

import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.lazy
import io.vertx.example.Injection
import org.jooq.*
import io.vertx.example.jooq.Tables.*
import io.vertx.example.jooq.tables.pojos.VertxUser

interface UserRepository {
    fun findUsers(): List<VertxUser>

    fun findUserById(id: Int): VertxUser?
}

class UserRepositoryImpl() : UserRepository {
    private val ctx: DSLContext by Injection.lazy.instance()

    override fun findUsers(): List<VertxUser> {
        return ctx.select().from(VERTX_USER).fetchInto(VertxUser::class.java)
    }

    override fun findUserById(id: Int): VertxUser? {
        return ctx.select().from(VERTX_USER).where(VERTX_USER.ID.eq(id)).fetchOneInto(VertxUser::class.java)
    }
}
