package io.vertx.example.repositories

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.mingchuno.mongopb4s.test.v3.TestV3.MyTestV3
import com.google.protobuf.*
import com.google.protobuf.Descriptors.FieldDescriptor
import com.mongodb.async.client.MongoClients
import com.mongodb.async.client.MongoCollection
import com.mongodb.async.client.MongoDatabase
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.*
import org.bson.codecs.configuration.CodecRegistries
import org.litote.kmongo.async.KMongo

class PBCodecBuilder(val includingDefaultValueFields: Set<Descriptors.FieldDescriptor> = setOf(),
                     val preservingProtoFieldNames: Boolean = false) {

  fun <T: Message>getCodecFor(clazz: Class<T>): Codec<T> {
    return object: Codec<T> {
      override fun getEncoderClass(): Class<T> = clazz

      override fun encode(writer: BsonWriter, value: T, encoderContext: EncoderContext) {
        WriterImpl(
          includingDefaultValueFields = includingDefaultValueFields,
          preservingProtoFieldNames = preservingProtoFieldNames,
          writer = writer,
          alwaysOutputDefaultValueFields = true
        ).write(value)
      }

      override fun decode(reader: BsonReader, decoderContext: DecoderContext): T {
        val builder = clazz.getDeclaredMethod("newBuilder").invoke(null) as com.google.protobuf.Message.Builder


        @Suppress("unchecked_cast")
        return parseObj(reader, builder) as T
      }

      private fun parseObj(reader: BsonReader, builder: Message.Builder): Any {
        when(reader.currentBsonType){
          BsonType.DOCUMENT -> {
            println("working in document {$builder}")
            reader.readStartDocument()
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
              parseValue(reader, builder)
            }
            reader.readEndDocument()
          }
          BsonType.ARRAY -> {
            reader.readStartArray()
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
              parseValue(reader, builder)
            }
            reader.readEndArray()
          }
          BsonType.END_OF_DOCUMENT -> {}
          else -> {
            parseValue(reader, builder)
          }
        }
        println("finish process one Object...")
        return builder.build()
      }

      private fun parseValue(reader: BsonReader, builder: Message.Builder) {
        val typeDes = builder.descriptorForType
        val name = reader.readName()
        val snakeName = Regex("[A-Z]").replace(name) { "_${it.value.toLowerCase()}" }
        val descriptor: FieldDescriptor? = typeDes.findFieldByName(snakeName)

        println("working in descriptor: $descriptor, name: $name, $snakeName: $snakeName with currentBsonType: ${reader.currentBsonType}")
        if(descriptor != null){
          when {
            descriptor.isMapField -> when(reader.currentBsonType){
              BsonType.DOCUMENT -> {
                println("working in parseValue DOCUMENT: $name")
                val mapEntryDesc = descriptor.messageType
                val keyDescriptor = mapEntryDesc.findFieldByNumber(1)
                val valueDescriptor = mapEntryDesc.findFieldByNumber(2)
                reader.readStartDocument()

                while(reader.readBsonType() != BsonType.END_OF_DOCUMENT){
                  val key = reader.readName()
                  val keyObj: Any = when(keyDescriptor.javaType){
                    FieldDescriptor.JavaType.INT -> key.toInt()
                    FieldDescriptor.JavaType.LONG -> key.toLong()
                    FieldDescriptor.JavaType.FLOAT -> key.toFloat()
                    FieldDescriptor.JavaType.DOUBLE -> key.toDouble()
                    FieldDescriptor.JavaType.BOOLEAN -> key.toBoolean()
                    FieldDescriptor.JavaType.STRING -> key
                    FieldDescriptor.JavaType.BYTE_STRING -> ByteString.copyFrom(key.toByteArray())
                    else -> TODO("throw unsupported type for key")
                  }

                  val value = parseSingleValue(reader, valueDescriptor)

                  val entryBuilder = builder.newBuilderForField(descriptor)
                  entryBuilder.setField(keyDescriptor, keyObj)
                  entryBuilder.setField(valueDescriptor, value)

                  println("working in document $descriptor, key-value: ($key,$value)")

                  builder.addRepeatedField(descriptor, entryBuilder.build())
                }
                reader.readEndDocument()
              }
              else -> {
                TODO("Expected an object for map field name=$name")
              }
            }
            descriptor.isRepeated -> when(reader.currentBsonType){
              BsonType.ARRAY -> {
                println("working in parseValue array: $name")

                reader.readStartArray()
                while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {

                  val value = parseSingleValue(reader, descriptor)
                  println("working in array, value: $value")
                  builder.addRepeatedField(descriptor,value)

                }
                reader.readEndArray()
              }
              else -> {
                TODO("Expected an array for repeated field name=$name")
              }
            }
            else -> {
              val value: Any = parseSingleValue(reader, descriptor)
              println("working in parseValue single: $name with value: $value")

              builder.setField(descriptor, value)
            }
          }
        } else {
          reader.skipValue()
        }
      }

      private fun parseSingleValue(reader: BsonReader, descriptor: FieldDescriptor): Any {
        return when(descriptor.javaType){
          FieldDescriptor.JavaType.INT -> reader.readInt32()
          FieldDescriptor.JavaType.LONG -> reader.readInt64()
          FieldDescriptor.JavaType.FLOAT -> reader.readDouble().toFloat()
          FieldDescriptor.JavaType.DOUBLE -> reader.readDouble()
          FieldDescriptor.JavaType.BOOLEAN -> reader.readBoolean()
          FieldDescriptor.JavaType.STRING -> reader.readString()
          FieldDescriptor.JavaType.BYTE_STRING -> ByteString.copyFrom(reader.readBinaryData().data)
          FieldDescriptor.JavaType.ENUM -> descriptor.enumType.findValueByName(reader.readString())
          FieldDescriptor.JavaType.MESSAGE -> {
            when(descriptor.messageType.fullName){
              com.google.protobuf.Int32Value.getDescriptor().fullName -> {
                Int32Value.newBuilder().setValue(reader.readInt32()).build()
              }
              com.google.protobuf.UInt32Value.getDescriptor().fullName -> {
                UInt32Value.newBuilder().setValue(reader.readInt32()).build()
              }
              com.google.protobuf.UInt64Value.getDescriptor().fullName -> {
                UInt64Value.newBuilder().setValue(reader.readInt64()).build()
              }
              com.google.protobuf.Int64Value.getDescriptor().fullName -> {
                Int64Value.newBuilder().setValue(reader.readInt64()).build()
              }
              com.google.protobuf.BoolValue.getDescriptor().fullName -> {
                BoolValue.newBuilder().setValue(reader.readBoolean()).build()
              }
              com.google.protobuf.StringValue.getDescriptor().fullName -> {
                StringValue.newBuilder().setValue(reader.readString()).build()
              }
              com.google.protobuf.DoubleValue.getDescriptor().fullName -> {
                DoubleValue.newBuilder().setValue(reader.readDouble()).build()
              }
              com.google.protobuf.BytesValue.getDescriptor().fullName -> {
                BytesValue.newBuilder().setValue(ByteString.copyFrom(reader.readBinaryData().data)).build()
              }
              com.google.protobuf.FloatValue.getDescriptor().fullName -> {
                FloatValue.newBuilder().setValue(reader.readDouble().toFloat()).build()
              }
              else -> {
                val subBuilder = DynamicMessage.newBuilder(descriptor.messageType)
                parseObj(reader, subBuilder)
              }
            }

          }
          else -> TODO("throw unsupported type for key")
        }
      }

    }
  }
}

class MyTestV3Respository: MongoPBRepository<MyTestV3>("test") {
  override val collection: MongoCollection<MyTestV3> = getCollectionWithCodec("mytestv3")
}

abstract class MongoPBRepository<T: Message>(databaseName: String): MongoRepository<T>(databaseName) {
  inline fun <reified T: Message> getCollectionWithCodec(collectionName: String): MongoCollection<T> {
    val codec: Codec<T> = PBCodecBuilder().getCodecFor(T::class.java)
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

abstract class ProtoMessageMixin {
  @JsonIgnore
  abstract fun getUnknownFields(): Any

  @JsonIgnore
  abstract fun getParserForType(): Any

  @JsonIgnore
  abstract fun getDefaultInstanceForType(): Any

  @JsonIgnore
  abstract fun getOptMessage(): Any

  @JsonIgnore
  abstract fun getOptMessageOrBuilder(): Any

}
