package com.acme.transport.crewing.dutyperiod.schedule

/** How screens and exports show a batched duty period schedule. */
data class BatchedDutyPeriodScheduleView(
    var codeText: String,
    var title: String,
    var budget: String,
    var scoreText: String,
    var category: String,
    var pinned: String,
    var statusLabel: String,
)

fun toBatchedDutyPeriodScheduleView(item: BatchedDutyPeriodSchedule): BatchedDutyPeriodScheduleView {
    return BatchedDutyPeriodScheduleView(
        codeText = item.code,
        title = item.title,
        budget = "%.2f".format(item.budget),
        scoreText = "%.2f".format(item.score),
        category = item.category,
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDutyPeriodScheduleViews(items: MutableList<BatchedDutyPeriodSchedule>): MutableList<BatchedDutyPeriodScheduleView> {
    val views = mutableListOf<BatchedDutyPeriodScheduleView>()
    for (item in items) {
        views.add(toBatchedDutyPeriodScheduleView(item))
    }
    return views
}

fun sumBatchedDutyPeriodScheduleView(view: BatchedDutyPeriodScheduleView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedScore = view.scoreText.toDouble()
    return parsedBudget + parsedScore
}
