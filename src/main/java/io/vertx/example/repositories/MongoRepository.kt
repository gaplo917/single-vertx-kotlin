package io.vertx.example.repositories

import com.google.protobuf.*
import com.mongodb.async.client.MongoClients
import com.mongodb.async.client.MongoCollection
import com.mongodb.async.client.MongoDatabase
import io.vertx.example.foundation.protobuf.PBCodec
import org.bson.codecs.*
import org.bson.codecs.configuration.CodecRegistries
import org.litote.kmongo.async.KMongo


abstract class MongoPBRepository<T: Message>(databaseName: String): MongoRepository<T>(databaseName) {
  inline fun <reified T: Message> getCollectionWithCodec(collectionName: String): MongoCollection<T> {
    val codec: Codec<T> = PBCodec(clazz = T::class.java)
    val registry = CodecRegistries.fromRegistries(
      CodecRegistries.fromCodecs(codec),
      MongoClients.getDefaultCodecRegistry(),
      CodecRegistries.fromProviders(DocumentCodecProvider(), IterableCodecProvider())
    )
    return `access$database`.withCodecRegistry(registry).getCollection(collectionName, T::class.java)
  }

  @PublishedApi
  internal val `access$database`: MongoDatabase get() = database

}

abstract class MongoRepository<T: Any>(databaseName: String) {
  private val client = KMongo.createClient() //get com.mongodb.async.client.MongoClient new instance
  protected val database: MongoDatabase = client.getDatabase(databaseName)  //normal java driver usage
  protected abstract val collection: MongoCollection<T> //KMongo extension method

}
