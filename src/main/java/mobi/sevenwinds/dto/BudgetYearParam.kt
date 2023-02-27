package mobi.sevenwinds.dto

import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam

data class BudgetYearParam(
    @PathParam("Год") val year: Int,
    @QueryParam("Фильтр по ФИО") val filter: String?,
    @QueryParam("Лимит пагинации") val limit: Int,
    @QueryParam("Смещение пагинации") val offset: Int,
)