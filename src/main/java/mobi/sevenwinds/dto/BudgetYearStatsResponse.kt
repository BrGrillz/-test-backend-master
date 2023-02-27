package mobi.sevenwinds.dto


class BudgetYearStatsResponse(
    val total: Int,
    val totalByType: Map<String, Int>,
    var items: List<BudgetRecordResponse>
)