import com.github.mingchuno.mongopb4s.test.v3.TestV3
import com.google.protobuf.ByteString
import com.mongodb.async.client.MongoCollection
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FeatureSpec
import io.vertx.example.repositories.MongoPBRepository
import kotlinx.coroutines.experimental.runBlocking
import org.litote.kmongo.coroutine.findOne
import org.litote.kmongo.coroutine.insertOne
import java.util.*

class MongoMyTestPBSepc : FeatureSpec() {
    companion object {
        val bytes = ByteArray(10).also { Random().nextBytes(it) }
        val byteString = ByteString.copyFrom(bytes)

        val _hello = "world"
        val _foobar = Int.MAX_VALUE
        val _bazinga = Long.MAX_VALUE
        val _optEnum = TestV3.MyEnumV3.V2
        val _optBs = byteString
        val _optBool = true
        val _optDouble = Double.MAX_VALUE
        val _optFloat = Float.MAX_VALUE
        val _primitiveSequenceList = listOf("a","b","c")
        val _repMessageList = listOf(TestV3.MyTestV3.getDefaultInstance())
        val _stringToInt32Map = mapOf("mingchuno" to 12345)
        val _intToMytestMap = mapOf(1 to TestV3.MyTestV3.getDefaultInstance())
        val _repEnumList = listOf(TestV3.MyEnumV3.V1, TestV3.MyEnumV3.V2)
        val _intToEnumMap = mapOf(1 to TestV3.MyEnumV3.V2)
        val _boolToStringMap = mapOf(true to "mingchuno")
        val _stringToBoolMap = mapOf("mingchuno" to true)
        val _fixed64ToBytesMap = mapOf(Long.MIN_VALUE to ByteString.copyFrom(bytes))
    }
    init {
        val repo = object: MongoPBRepository<TestV3.MyTestV3>("test") {
            override val collection: MongoCollection<TestV3.MyTestV3> = getCollectionWithCodec("prototest")
            suspend fun insertOne(test: TestV3.MyTestV3){
                collection.insertOne(test)
            }

            suspend fun findOne(): TestV3.MyTestV3? {
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
                    val proto = TestV3.MyTestV3.newBuilder().apply {
                        hello = _hello
                        foobar = _foobar
                        bazinga = _bazinga
                        optEnum = _optEnum
                        optBs = _optBs
                        optBool = _optBool
                        optDouble = _optDouble
                        optFloat = _optFloat

                        addAllPrimitiveSequence(_primitiveSequenceList)
                        addAllRepMessage(_repMessageList)
                        putAllStringToInt32(_stringToInt32Map)
                        putAllIntToMytest(_intToMytestMap)
                        addAllRepEnum(_repEnumList)
                        putAllIntToEnum(_intToEnumMap)
                        putAllBoolToString(_boolToStringMap)
                        putAllStringToBool(_stringToBoolMap)

                        putAllFixed64ToBytes(_fixed64ToBytesMap)

                    }.build()
                    repo.insertOne(proto)
                }
            }

            scenario("read proto from db"){
                runBlocking {
                    val proto = repo.findOne()!!

                    proto.hello shouldBe _hello
                    proto.foobar shouldBe _foobar
                    proto.bazinga shouldBe _bazinga
                    proto.optEnum shouldBe _optEnum
                    proto.optBs shouldBe _optBs
                    proto.optBool shouldBe _optBool
                    proto.optDouble shouldBe _optDouble
                    proto.optFloat shouldBe _optFloat
                    proto.primitiveSequenceList shouldBe _primitiveSequenceList
                    proto.repMessageList shouldBe _repMessageList
                    proto.stringToInt32Map shouldBe _stringToInt32Map
                    proto.intToMytestMap shouldBe _intToMytestMap
                    proto.repEnumList shouldBe _repEnumList
                    proto.intToEnumMap shouldBe _intToEnumMap
                    proto.boolToStringMap shouldBe _boolToStringMap
                    proto.stringToBoolMap shouldBe _stringToBoolMap
                    proto.fixed64ToBytesMap shouldBe _fixed64ToBytesMap

                }
            }
        }
    }
}
