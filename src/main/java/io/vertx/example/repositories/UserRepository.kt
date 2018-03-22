package io.vertx.example.repositories

import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.lazy
import io.vertx.example.Injection
import org.jooq.*
import org.jooq.impl.*
import io.vertx.example.jooq.Tables.*
import io.vertx.example.jooq.tables.pojos.VertxUser
import io.vertx.example.jooq.tables.records.VertxUserRecord

interface UserRepository {

    fun createUser(name: String): VertxUser

    fun updateUser(id: Int, name: String): VertxUser

    fun findUsers(): List<VertxUser>

    fun findUserById(id: Int): VertxUser?
}

class UserRepositoryImpl() : UserRepository {
    private val ctx: DSLContext by Injection.lazy.instance()

    override fun createUser(name: String): VertxUser {
        val userRecord = ctx.newRecord(VERTX_USER)

        userRecord.name = name

        userRecord.store()

        return userRecord.original().into(VertxUser::class.java)
    }

    override fun updateUser(id: Int, name: String): VertxUser {
        val userRecord = recordById(id)

        userRecord.name = name

        userRecord.update()

        return userRecord.original().into(VertxUser::class.java)
    }

    override fun findUsers(): List<VertxUser> {
        // OR ctx.select().from(VERTX_USER).fetchInto(VertxUser::class.java)
        return ctx.fetch(VERTX_USER).into(VertxUser::class.java)
    }

    override fun findUserById(id: Int): VertxUser? {
        // OR ctx.select().from(VERTX_USER).where(VERTX_USER.ID.eq(id)).fetchOneInto(VertxUser::class.java)
        return recordById(id).into(VertxUser::class.java)
    }

    private fun recordById(id: Int): VertxUserRecord {
        return ctx.fetchOne(VERTX_USER, VERTX_USER.ID.eq(id))
    }
}