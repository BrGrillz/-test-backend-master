package mobi.sevenwinds.app.budget

import io.ktor.features.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import mobi.sevenwinds.dto.BudgetRecordRequest
import mobi.sevenwinds.dto.BudgetRecordResponse
import mobi.sevenwinds.dto.BudgetYearParam
import mobi.sevenwinds.dto.BudgetYearStatsResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecordRequest): BudgetRecordResponse = withContext(Dispatchers.IO) {

        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = if (body.authorId != null) getAuthor(body.authorId) else null
            }

            return@transaction entity.toResponse()
        }
    }

    private fun getAuthor(authorId: Int): AuthorEntity {
        val response = AuthorEntity.wrapRows(
            transaction {
                AuthorTable.select{
                    AuthorTable.id eq authorId
                }
            }
        )

        if (response.count() != 1)
            throw NotFoundException("Author not found")

        return response.take(1).get(0)
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = BudgetTable.join(
                AuthorTable,
                JoinType.LEFT,
            ).select{
                BudgetTable.year eq param.year
            }
                .limit(param.limit, param.offset)

            val total = query.count()
            val data = BudgetEntity.wrapRows(query)
                .map { it.toResponse() }
                .filter { param.filter == null || it.fio?.contains(param.filter, true) == true }

            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}
