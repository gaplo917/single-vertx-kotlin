package io.vertx.example.foundation.protobuf

import com.google.protobuf.*
import org.bson.BsonReader
import org.bson.BsonType

private typealias WellKnownTypeReader = (BsonReader) -> Any

class PBBsonReader(
        private val reader: BsonReader,
        private val preservedProtoFieldNames: Boolean = false
) {

    fun <T> read(clazz: Class<T>): T {
        val builder = clazz.getDeclaredMethod("newBuilder").invoke(null) as com.google.protobuf.Message.Builder

        @Suppress("unchecked_cast")
        return parseObj(reader, builder) as T
    }

    private fun parseObj(reader: BsonReader, builder: Message.Builder): Any {
        when(reader.currentBsonType){
            BsonType.DOCUMENT -> {
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
        return builder.build()
    }

    private fun parseValue(reader: BsonReader, builder: Message.Builder) {
        val typeDes = builder.descriptorForType
        val name = reader.readName()
        val snakeName = if(preservedProtoFieldNames) name else Regex("[A-Z]").replace(name) { "_${it.value.toLowerCase()}" }
        val descriptor: Descriptors.FieldDescriptor? = typeDes.findFieldByName(snakeName)

        if(descriptor != null){
            when {
                descriptor.isMapField -> when(reader.currentBsonType){
                    BsonType.DOCUMENT -> {
                        val mapEntryDesc = descriptor.messageType
                        val keyDescriptor = mapEntryDesc.findFieldByNumber(1)
                        val valueDescriptor = mapEntryDesc.findFieldByNumber(2)
                        reader.readStartDocument()

                        while(reader.readBsonType() != BsonType.END_OF_DOCUMENT){
                            val key = reader.readName()
                            val keyObj: Any = when(keyDescriptor.javaType){
                                Descriptors.FieldDescriptor.JavaType.INT -> key.toInt()
                                Descriptors.FieldDescriptor.JavaType.LONG -> key.toLong()
                                Descriptors.FieldDescriptor.JavaType.FLOAT -> key.toFloat()
                                Descriptors.FieldDescriptor.JavaType.DOUBLE -> key.toDouble()
                                Descriptors.FieldDescriptor.JavaType.BOOLEAN -> key.toBoolean()
                                Descriptors.FieldDescriptor.JavaType.STRING -> key
                                Descriptors.FieldDescriptor.JavaType.BYTE_STRING -> ByteString.copyFrom(key.toByteArray())
                                else -> TODO("throw unsupported type for key")
                            }

                            val value = parseSingleValue(reader, valueDescriptor)

                            val entryBuilder = builder.newBuilderForField(descriptor)
                            entryBuilder.setField(keyDescriptor, keyObj)
                            entryBuilder.setField(valueDescriptor, value)

                            builder.addRepeatedField(descriptor, entryBuilder.build())
                        }
                        reader.readEndDocument()
                    }
                    else -> {
                        throw PBCodecDecodeException("Expected an object for map field name=$name")
                    }
                }
                descriptor.isRepeated -> when(reader.currentBsonType){
                    BsonType.ARRAY -> {

                        reader.readStartArray()
                        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {

                            val value = parseSingleValue(reader, descriptor)

                            builder.addRepeatedField(descriptor,value)

                        }
                        reader.readEndArray()
                    }
                    else -> {
                        throw PBCodecDecodeException("Expected an array for repeated field name=$name")
                    }
                }
                else -> {
                    val value: Any = parseSingleValue(reader, descriptor)

                    builder.setField(descriptor, value)
                }
            }
        } else {
            reader.skipValue()
        }
    }

    private fun parseSingleValue(reader: BsonReader, descriptor: Descriptors.FieldDescriptor): Any {
        return when(descriptor.javaType){
            Descriptors.FieldDescriptor.JavaType.INT -> reader.readInt32()
            Descriptors.FieldDescriptor.JavaType.LONG -> reader.readInt64()
            Descriptors.FieldDescriptor.JavaType.FLOAT -> reader.readDouble().toFloat()
            Descriptors.FieldDescriptor.JavaType.DOUBLE -> reader.readDouble()
            Descriptors.FieldDescriptor.JavaType.BOOLEAN -> reader.readBoolean()
            Descriptors.FieldDescriptor.JavaType.STRING -> reader.readString()
            Descriptors.FieldDescriptor.JavaType.BYTE_STRING -> ByteString.copyFrom(reader.readBinaryData().data)
            Descriptors.FieldDescriptor.JavaType.ENUM -> descriptor.enumType.findValueByName(reader.readString())
            Descriptors.FieldDescriptor.JavaType.MESSAGE -> {
                val specialReader: WellKnownTypeReader? = wellKnownTypeReaders[descriptor.messageType.fullName]

                return if(specialReader != null){
                    specialReader.invoke(reader)
                } else {
                    val subBuilder = DynamicMessage.newBuilder(descriptor.messageType)
                    parseObj(reader, subBuilder)
                }

            }
            else -> throw PBCodecDecodeException("Unsupported java type in descriptor=$descriptor")
        }
    }


    companion object {

        private val wellKnownTypeReaders = buildWellKnownTypeReaders()

        private fun buildWellKnownTypeReaders(): Map<String, WellKnownTypeReader> {
            val readers = mutableMapOf<String, WellKnownTypeReader>()

            readers[BoolValue.getDescriptor().fullName] = { reader ->
                BoolValue.newBuilder().setValue(reader.readBoolean()).build()
            }
            readers[Int32Value.getDescriptor().fullName] = { reader ->
                Int32Value.newBuilder().setValue(reader.readInt32()).build()
            }
            readers[UInt32Value.getDescriptor().fullName] = { reader ->
                UInt32Value.newBuilder().setValue(reader.readInt32()).build()
            }
            readers[Int64Value.getDescriptor().fullName] = { reader ->
                Int64Value.newBuilder().setValue(reader.readInt64()).build()
            }
            readers[UInt64Value.getDescriptor().fullName] = { reader ->
                UInt64Value.newBuilder().setValue(reader.readInt64()).build()
            }
            readers[StringValue.getDescriptor().fullName] = { reader ->
                StringValue.newBuilder().setValue(reader.readString()).build()
            }
            readers[BytesValue.getDescriptor().fullName] = { reader ->
                BytesValue.newBuilder().setValue(ByteString.copyFrom(reader.readBinaryData().data)).build()
            }
            readers[FloatValue.getDescriptor().fullName] = { reader ->
                FloatValue.newBuilder().setValue(reader.readDouble().toFloat()).build()
            }
            readers[DoubleValue.getDescriptor().fullName] = { reader ->
                DoubleValue.newBuilder().setValue(reader.readDouble()).build()
            }

            return readers
        }
    }
}
