import com.mongodb.async.client.MongoCollection
import io.kotlintest.specs.FeatureSpec
import io.vertx.example.repositories.MongoRepository
import kotlinx.coroutines.experimental.runBlocking
import org.litote.kmongo.coroutine.getCollection
import org.litote.kmongo.coroutine.insertOne
import java.math.BigDecimal

enum class TestEnum { A, B, C; }

data class Test(
        val _1: Int,
        val _2: Long,
        val _3: String,
        val _4: BigDecimal,
        val _5: TestEnum,
        var _6: Boolean,
        var _7: Float,
        var _8: Double,
        var _9: Short,
        var _10: List<Int>,
        var _11: Map<String, String>
)

data class NestedClass(val nested: Boolean)

class MongoSpec: FeatureSpec() {
    init {
        feature("kmongo"){
            scenario("save test class"){
                val repo = object: MongoRepository<Test>("test") {
                    override val collection: MongoCollection<Test> = database.getCollection()
                    suspend fun insertOne(test: Test){
                        collection.insertOne(test)
                    }
                }
                runBlocking {
                    val test = Test(
                            _1 = 1,
                            _2 = 1L,
                            _3 = "test",
                            _4 = BigDecimal.ONE,
                            _5 = TestEnum.A,
                            _6 = true,
                            _7 = Float.MAX_VALUE,
                            _8 = Double.MAX_VALUE,
                            _9 = 1,
                            _10 = listOf(1, 2, 3, 4, 5),
                            _11 = mapOf("a" to "b")
                    )
                    repo.insertOne(test)
                }
            }

        }
    }
}
