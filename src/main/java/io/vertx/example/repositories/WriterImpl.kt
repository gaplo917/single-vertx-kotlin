package io.vertx.example.repositories

import com.google.common.io.BaseEncoding
import com.google.protobuf.*
import com.google.protobuf.util.Durations
import com.google.protobuf.util.FieldMaskUtil
import com.google.protobuf.Any as PBAny
import com.google.protobuf.util.JsonFormat
import org.bson.BsonBinary
import org.bson.BsonWriter
import java.io.IOException
import java.util.*

internal class WriterImpl(
  private val alwaysOutputDefaultValueFields: Boolean,
  private val includingDefaultValueFields: Set<Descriptors.FieldDescriptor>,
  private val preservingProtoFieldNames: Boolean,
  private val writer: BsonWriter
) {

  @Throws(IOException::class)
  fun write(message: MessageOrBuilder) {
    val specialWriter = wellKnownTypeWriters[message.descriptorForType.fullName]
    if (specialWriter != null) {
      specialWriter.write(this, message)
      return
    }
    write(message, null)
  }

  private interface WellKnownTypeWriter {
    @Throws(IOException::class)
    fun write(writer: WriterImpl, message: MessageOrBuilder)
  }

  /** Prints google.protobuf.Any  */
  @Throws(IOException::class)
  private fun writeAny(message: MessageOrBuilder) {
    if (PBAny.getDefaultInstance() == message) {
      writer.writeStartDocument()
      writer.writeEndDocument()
      return
    }
    val descriptor = message.descriptorForType
    val typeUrlField = descriptor.findFieldByName("type_url")
    val valueField = descriptor.findFieldByName("value")
    // Validates type of the message. Note that we can't just cast the message
    // to com.google.protobuf.Any because it might be a DynamicMessage.
    if (typeUrlField == null
      || valueField == null
      || typeUrlField.type != Descriptors.FieldDescriptor.Type.STRING
      || valueField.type != Descriptors.FieldDescriptor.Type.BYTES) {
      throw InvalidProtocolBufferException("Invalid Any type.")
    }
    val typeUrl = message.getField(typeUrlField) as String
    val value = message.getField(valueField) as ByteString
    writer.writeStartDocument()
    writer.writeString("typeUrl", typeUrl)
    writer.writeBinaryData("value", BsonBinary(value.toByteArray()))
    writer.writeEndDocument()
  }

  /** Prints wrapper types (e.g., google.protobuf.Int32Value)  */
  @Throws(IOException::class)
  private fun writeWrapper(message: MessageOrBuilder) {
    val descriptor = message.descriptorForType
    val valueField = descriptor.findFieldByName("value") ?: throw InvalidProtocolBufferException("Invalid Wrapper type.")
    // When formatting wrapper types, we just write its value field instead of
    // the whole message.
    writeSingleFieldValue(valueField, message.getField(valueField))
  }

  private fun toByteString(message: MessageOrBuilder): ByteString {
    return if (message is Message) {
      message.toByteString()
    } else {
      (message as Message.Builder).build().toByteString()
    }
  }

//  /** Prints google.protobuf.Timestamp  */
//  @Throws(IOException::class)
//  private fun writeTimestamp(message: MessageOrBuilder) {
//    val value = Timestamp.parseFrom(toByteString(message))
//    writer.writeStartDocument()
//    writer.writeInt64("seconds", value.seconds)
//    writer.writeInt32("nanos", value.nanos)
//    writer.writeEndDocument()
//  }
//
//  /** Prints google.protobuf.Duration  */
//  @Throws(IOException::class)
//  private fun writeDuration(message: MessageOrBuilder) {
//    val value = Duration.parseFrom(toByteString(message))
//    writer.writeString(Durations.toString(value))
//  }

  /** Prints google.protobuf.FieldMask  */
  @Throws(IOException::class)
  private fun writeFieldMask(message: MessageOrBuilder) {
    val value = FieldMask.parseFrom(toByteString(message))
    writer.writeString(FieldMaskUtil.toJsonString(value))
  }

  /** Prints google.protobuf.Struct  */
  @Throws(IOException::class)
  private fun writeStruct(message: MessageOrBuilder) {
    val descriptor = message.descriptorForType
    val field = descriptor.findFieldByName("fields") ?: throw InvalidProtocolBufferException("Invalid Struct type.")
    // Struct is formatted as a map object.
    writeMapFieldValue(field, message.getField(field))
  }

  /** Prints google.protobuf.Value  */
  @Throws(IOException::class)
  private fun writeValue(message: MessageOrBuilder) {
    // For a Value message, only the value of the field is formatted.
    val fields = message.allFields
    if (fields.isEmpty()) {
      // No value set.
      writer.writeNull()
      return
    }
    // A Value message can only have at most one field set (it only contains
    // an oneof).
    if (fields.size != 1) {
      throw InvalidProtocolBufferException("Invalid Value type.")
    }
    for ((key, value) in fields) {
      writeSingleFieldValue(key, value)
    }
  }

  /** Prints google.protobuf.ListValue  */
  @Throws(IOException::class)
  private fun writeListValue(message: MessageOrBuilder) {
    val descriptor = message.descriptorForType
    val field = descriptor.findFieldByName("values") ?: throw InvalidProtocolBufferException("Invalid ListValue type.")
    writeRepeatedFieldValue(field, message.getField(field))
  }

  /** Prints a regular message with an optional type URL.  */
  @Throws(IOException::class)
  private fun write(message: MessageOrBuilder, typeUrl: String?) {
    writer.writeStartDocument()

    if (typeUrl != null) {
      writer.writeString("typeUrl", typeUrl)
    }

    val fieldsToWrite: MutableMap<Descriptors.FieldDescriptor, Any>
    if (alwaysOutputDefaultValueFields || !includingDefaultValueFields.isEmpty()) {
      fieldsToWrite = TreeMap(message.allFields)
      for (field in message.descriptorForType.fields) {
        if (field.isOptional) {
          if (field.javaType == Descriptors.FieldDescriptor.JavaType.MESSAGE && !message.hasField(field)) {
            // Always skip empty optional message fields. If not we will recurse indefinitely if
            // a message has itself as a sub-field.
            continue
          }
          val oneof = field.containingOneof
          if (oneof != null && !message.hasField(field)) {
            // Skip all oneof fields except the one that is actually set
            continue
          }
        }
        if (!fieldsToWrite.containsKey(field) && (alwaysOutputDefaultValueFields || includingDefaultValueFields.contains(field))) {
          fieldsToWrite.put(field, message.getField(field))
        }
      }
    } else {
      fieldsToWrite = message.allFields
    }
    for ((key, value) in fieldsToWrite) {
      writeField(key, value)
    }
    writer.writeEndDocument()
  }

  @Throws(IOException::class)
  private fun writeField(field: Descriptors.FieldDescriptor, value: Any) {
    if (preservingProtoFieldNames) {
      writer.writeName(field.name)
    } else {
      writer.writeName(field.jsonName)
    }
    when {
        field.isMapField -> writeMapFieldValue(field, value)
        field.isRepeated -> writeRepeatedFieldValue(field, value)
        else -> writeSingleFieldValue(field, value)
    }
  }

  @Throws(IOException::class)
  private fun writeRepeatedFieldValue(field: Descriptors.FieldDescriptor, value: Any) {
    writer.writeStartArray()
    @Suppress("unchecked_cast")
    for (element in value as List<Any>) {
      writeSingleFieldValue(field, element)
    }
    writer.writeEndArray()
  }

  @Throws(IOException::class)
  private fun writeMapFieldValue(field: Descriptors.FieldDescriptor, value: Any) {
    val type = field.messageType
    val keyField = type.findFieldByName("key")
    val valueField = type.findFieldByName("value")
    if (keyField == null || valueField == null) {
      throw InvalidProtocolBufferException("Invalid map field.")
    }
    writer.writeStartDocument()

    for (element in value as List<*>) {
      val entry = element as Message
      val entryKey = entry.getField(keyField)
      val entryValue = entry.getField(valueField)

      // Key fields are always double-quoted.
      writeSingleFieldValue(keyField, entryKey, isWritingName = true)
      writeSingleFieldValue(valueField, entryValue)
    }

    writer.writeEndDocument()
  }

  /**
   * Prints a field's value in JSON format.
   *
   * @param isWritingName whether to always add double-quotes to primitive
   * types.
   */
  @Throws(IOException::class)
  private fun writeSingleFieldValue(
    field: Descriptors.FieldDescriptor, value: Any, isWritingName: Boolean = false) {
    when (field.type) {
      Descriptors.FieldDescriptor.Type.INT32, Descriptors.FieldDescriptor.Type.SINT32, Descriptors.FieldDescriptor.Type.SFIXED32, Descriptors.FieldDescriptor.Type.UINT32, Descriptors.FieldDescriptor.Type.FIXED32 -> {
        if (isWritingName) {
          writer.writeName((value as Int).toString())
        } else {
          writer.writeInt32(value as Int)
        }
      }

      Descriptors.FieldDescriptor.Type.INT64, Descriptors.FieldDescriptor.Type.SINT64, Descriptors.FieldDescriptor.Type.SFIXED64, Descriptors.FieldDescriptor.Type.UINT64, Descriptors.FieldDescriptor.Type.FIXED64 -> {
        if (isWritingName) {
          writer.writeName((value as Long).toString())
        } else {
          writer.writeInt64(value as Long)
        }
      }

      Descriptors.FieldDescriptor.Type.BOOL -> {
        if (isWritingName) {
          writer.writeName((value as Boolean).toString())
        } else {
          writer.writeBoolean(value as Boolean)
        }
      }

      Descriptors.FieldDescriptor.Type.FLOAT -> {
        val floatValue = value as Float
        if (floatValue.isNaN()) {
          writer.writeDouble(Double.NaN)
        } else if (floatValue.isInfinite()) {
          if (floatValue < 0) {
            writer.writeDouble(Double.NEGATIVE_INFINITY)
          } else {
            writer.writeDouble(Double.POSITIVE_INFINITY)
          }
        } else {
          if (isWritingName) {
            writer.writeName((value).toString())
          } else {
            writer.writeDouble(value.toDouble())
          }
        }
      }

      Descriptors.FieldDescriptor.Type.DOUBLE -> {
        val floatValue = value as Double
        if (floatValue.isNaN()) {
          writer.writeDouble(Double.NaN)
        } else if (floatValue.isInfinite()) {
          if (floatValue < 0) {
            writer.writeDouble(Double.NEGATIVE_INFINITY)
          } else {
            writer.writeDouble(Double.POSITIVE_INFINITY)
          }
        } else {
          if (isWritingName) {
            writer.writeName((value).toString())
          } else {
            writer.writeDouble(value)
          }
        }
      }
        Descriptors.FieldDescriptor.Type.STRING -> {
        if(isWritingName){
          writer.writeName(value as String)
        } else {
          writer.writeString(value as String)
        }
      }

      Descriptors.FieldDescriptor.Type.BYTES -> {
        if(isWritingName){
          writer.writeName(BaseEncoding.base64().encode((value as ByteString).toByteArray()))
        } else {
          writer.writeBinaryData(BsonBinary((value as ByteString).toByteArray()))
        }
      }

      Descriptors.FieldDescriptor.Type.ENUM ->
        // Special-case google.protobuf.NullValue (it's an Enum).
        if (field.enumType.fullName == "google.protobuf.NullValue") {
          // No matter what value it contains, we always write it as "null".
          if (isWritingName) {
            writer.writeName("null")
          } else {
            writer.writeNull()
          }
        } else {
          if ((value as Descriptors.EnumValueDescriptor).index == -1) {
            if(isWritingName){
              writer.writeName(value.number.toString())
            } else {
              writer.writeInt32(value.number)
            }
          } else {
            if(isWritingName){
              writer.writeName(value.name)
            } else {
              writer.writeString(value.name)
            }
          }
        }

      Descriptors.FieldDescriptor.Type.MESSAGE, Descriptors.FieldDescriptor.Type.GROUP -> write(value as Message)
    }
  }

  companion object {

    private val wellKnownTypeWriters = buildWellKnownTypeWrites()

    private fun buildWellKnownTypeWrites(): Map<String, WellKnownTypeWriter> {
      val writers = HashMap<String, WellKnownTypeWriter>()
      // Special-case Any.
      writers.put(
        PBAny.getDescriptor().fullName,
        object : WellKnownTypeWriter {
          @Throws(IOException::class)
          override fun write(writer: WriterImpl, message: MessageOrBuilder) {
            writer.writeAny(message)
          }
        })
      // Special-case wrapper types.
      val wrappersWriter = object : WellKnownTypeWriter {
        @Throws(IOException::class)
        override fun write(writer: WriterImpl, message: MessageOrBuilder) {
          writer.writeWrapper(message)
        }
      }
      writers.put(BoolValue.getDescriptor().fullName, wrappersWriter)
      writers.put(Int32Value.getDescriptor().fullName, wrappersWriter)
      writers.put(UInt32Value.getDescriptor().fullName, wrappersWriter)
      writers.put(Int64Value.getDescriptor().fullName, wrappersWriter)
      writers.put(UInt64Value.getDescriptor().fullName, wrappersWriter)
      writers.put(StringValue.getDescriptor().fullName, wrappersWriter)
      writers.put(BytesValue.getDescriptor().fullName, wrappersWriter)
      writers.put(FloatValue.getDescriptor().fullName, wrappersWriter)
      writers.put(DoubleValue.getDescriptor().fullName, wrappersWriter)
      // Special-case Timestamp.
//      writers.put(
//        Timestamp.getDescriptor().fullName,
//        object : WellKnownTypeWriter {
//          @Throws(IOException::class)
//          override fun write(writer: WriterImpl, message: MessageOrBuilder) {
//            writer.writeTimestamp(message)
//          }
//        })
//      // Special-case Duration.
//      writers.put(
//        Duration.getDescriptor().fullName,
//        object : WellKnownTypeWriter {
//          @Throws(IOException::class)
//          override fun write(writer: WriterImpl, message: MessageOrBuilder) {
//            writer.writeDuration(message)
//          }
//        })
      // Special-case FieldMask.
      writers.put(
        FieldMask.getDescriptor().fullName,
        object : WellKnownTypeWriter {
          @Throws(IOException::class)
          override fun write(writer: WriterImpl, message: MessageOrBuilder) {
            writer.writeFieldMask(message)
          }
        })
      // Special-case Struct.
      writers.put(
        Struct.getDescriptor().fullName,
        object : WellKnownTypeWriter {
          @Throws(IOException::class)
          override fun write(writer: WriterImpl, message: MessageOrBuilder) {
            writer.writeStruct(message)
          }
        })
      // Special-case Value.
      writers.put(
        Value.getDescriptor().fullName,
        object : WellKnownTypeWriter {
          @Throws(IOException::class)
          override fun write(writer: WriterImpl, message: MessageOrBuilder) {
            writer.writeValue(message)
          }
        })
      // Special-case ListValue.
      writers.put(
        ListValue.getDescriptor().fullName,
        object : WellKnownTypeWriter {
          @Throws(IOException::class)
          override fun write(writer: WriterImpl, message: MessageOrBuilder) {
            writer.writeListValue(message)
          }
        })
      return writers
    }
  }
}
