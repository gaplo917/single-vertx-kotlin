import com.github.mingchuno.mongopb4s.test.v3.TestV3
import com.google.protobuf.*
import com.google.protobuf.Any
import com.google.protobuf.util.JsonFormat
import com.mongodb.async.client.MongoCollection
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FeatureSpec
import io.vertx.example.repositories.MongoPBRepository
import kotlinx.coroutines.experimental.runBlocking
import org.litote.kmongo.coroutine.findOne
import org.litote.kmongo.coroutine.insertOne
import java.util.*

class MongoMyTestSpecialPBSepc : FeatureSpec() {
    companion object {
        val bytes = ByteArray(10).also { Random().nextBytes(it) }
        val byteString = ByteString.copyFrom(bytes)

        val _seconds = 100L
        val _nanos = 100
        val testString = "Testing"
        val _any = Any.newBuilder().apply {
            typeUrl = "type.googleapis.com/${TestV3.MyTestV3.getDefaultInstance().descriptorForType.fullName}"
            value = TestV3.MyTestV3.getDefaultInstance().toByteString()
        }.build()

        val _timestamp = Timestamp.newBuilder().apply {
            seconds = _seconds
            nanos = _nanos
        }.build()

        val _duration = Duration.newBuilder().apply {
            seconds = _seconds
            nanos = _nanos
        }.build()

        val _struct = Struct.newBuilder().build()

        val _doubleVal = DoubleValue.newBuilder().setValue(1.0).build()

        val _floatVal = FloatValue.newBuilder().setValue(1.0f).build()

        val _int64Val = Int64Value.newBuilder().setValue(1L).build()
        val _uint64Val = UInt64Value.newBuilder().setValue(1L).build()
        val _int32Val = Int32Value.newBuilder().setValue(1).build()
        val _uint32Val = UInt32Value.newBuilder().setValue(1).build()
        val _boolVal = BoolValue.newBuilder().setValue(true).build()
        val _stringVal = StringValue.newBuilder().setValue(testString).build()
        val _bytesVal = BytesValue.newBuilder().setValue(byteString).build()
    }
    init {
        val repo = object: MongoPBRepository<TestV3.MyTestSpecial>("test") {
            override val collection: MongoCollection<TestV3.MyTestSpecial> = getCollectionWithCodec("prototest.special")
            suspend fun insertOne(test: TestV3.MyTestSpecial){
                collection.insertOne(test)
            }

            suspend fun findOne(): TestV3.MyTestSpecial? {
                return collection.findOne()
            }

            suspend fun drop() {
                collection.drop { result, t ->  }
            }
        }

        feature("mongodb with protobuf") {
            scenario("drop collection"){
                runBlocking {
                    repo.drop()
                }
            }
            scenario("save proto "){
                runBlocking {
                    val proto = TestV3.MyTestSpecial.newBuilder().apply {
                        emptyVal = Empty.newBuilder().build()
                        any = _any
                        timestamp = _timestamp
                        duration = _duration
                        struct = _struct
                        doubleVal = _doubleVal
                        floatVal = _floatVal
                        int64Val = _int64Val
                        uint64Val = _uint64Val
                        int32Val = _int32Val
                        uint32Val = _uint32Val
                        boolVal = _boolVal
                        stringVal = _stringVal
                        bytesVal = _bytesVal
                    }.build()

                    repo.insertOne(proto)
                }
            }

            scenario("read proto from db"){
                runBlocking {
                    val proto = repo.findOne()!!

                    proto.emptyVal shouldBe Empty.getDefaultInstance()
                    proto.any shouldBe _any
                    proto.timestamp shouldBe _timestamp
                    proto.duration shouldBe _duration
                    proto.struct shouldBe _struct
                    proto.doubleVal shouldBe _doubleVal
                    proto.floatVal shouldBe _floatVal
                    proto.int64Val shouldBe _int64Val
                    proto.uint64Val shouldBe _uint64Val
                    proto.int32Val shouldBe _int32Val
                    proto.uint32Val shouldBe _uint32Val
                    proto.boolVal shouldBe _boolVal
                    proto.stringVal shouldBe _stringVal
                    proto.bytesVal shouldBe _bytesVal

                }
            }
        }
    }
}
