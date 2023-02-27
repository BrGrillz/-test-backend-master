package mobi.sevenwinds.app.budget

import io.restassured.RestAssured
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import mobi.sevenwinds.dto.BudgetRecordRequest
import mobi.sevenwinds.dto.BudgetRecordResponse
import mobi.sevenwinds.dto.BudgetType
import mobi.sevenwinds.dto.BudgetYearStatsResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BudgetApiKtTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        transaction { BudgetTable.deleteAll() }
    }

    @Test
    fun testBudgetPagination() {
        addRecord(BudgetRecordRequest(2020, 5, 10, BudgetType.Приход, null))
        addRecord(BudgetRecordRequest(2020, 5, 5, BudgetType.Приход, null))
        addRecord(BudgetRecordRequest(2020, 5, 20, BudgetType.Приход, null))
        addRecord(BudgetRecordRequest(2020, 5, 30, BudgetType.Приход, 1))
        addRecord(BudgetRecordRequest(2020, 5, 40, BudgetType.Приход, 2))
        addRecord(BudgetRecordRequest(2030, 1, 1, BudgetType.Расход, null))

        RestAssured.given()
            .queryParam("limit", 5)
            .queryParam("offset", 0)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

                Assertions.assertEquals(5, response.total)
                Assertions.assertEquals(5, response.items.size)
                Assertions.assertEquals(105, response.totalByType[BudgetType.Приход.name])
            }
    }

    @Test
    fun testStatsSortOrder() {
        addRecord(BudgetRecordRequest(2020, 5, 100, BudgetType.Приход, null))
        addRecord(BudgetRecordRequest(2020, 1, 5, BudgetType.Приход, null))
        addRecord(BudgetRecordRequest(2020, 5, 50, BudgetType.Приход, 1))
        addRecord(BudgetRecordRequest(2020, 1, 30, BudgetType.Приход, 2))
        addRecord(BudgetRecordRequest(2020, 5, 400, BudgetType.Приход, null))

        // expected sort order - month ascending, amount descending

        RestAssured.given()
            .get("/budget/year/2020/stats?limit=100&offset=0")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                response.items = response.items
                    .sortedWith(compareBy( { it.month }, { -it.amount }))
                println(response.items)

                Assertions.assertEquals(30, response.items[0].amount)
                Assertions.assertEquals(5, response.items[1].amount)
                Assertions.assertEquals(400, response.items[2].amount)
                Assertions.assertEquals(100, response.items[3].amount)
                Assertions.assertEquals(50, response.items[4].amount)
            }
    }

    @Test
    fun testInvalidMonthValues() {
        RestAssured.given()
            .jsonBody(BudgetRecordRequest(2020, -5, 5, BudgetType.Приход, 2))
            .post("/budget/add")
            .then().statusCode(400)

        RestAssured.given()
            .jsonBody(BudgetRecordRequest(2020, 15, 5, BudgetType.Приход, 1))
            .post("/budget/add")
            .then().statusCode(400)
    }

    private fun addRecord(record: BudgetRecordRequest) {
        RestAssured.given()
            .jsonBody(record)
            .post("/budget/add")
            .toResponse<BudgetRecordResponse>()
    }
}