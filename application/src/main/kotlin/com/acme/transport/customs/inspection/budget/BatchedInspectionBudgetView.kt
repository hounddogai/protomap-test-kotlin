package com.acme.transport.customs.inspection.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched inspection budget. */
data class BatchedInspectionBudgetView(
    var code: String,
    var titleText: String,
    var routeCode: String,
    var approved: String,
    var unit: String,
    var categoryText: String,
    var attemptsText: String,
    var marginText: String,
    var name: String,
    var reviewerText: String,
    var statusLabel: String,
)

fun toBatchedInspectionBudgetView(item: BatchedInspectionBudget): BatchedInspectionBudgetView {
    return BatchedInspectionBudgetView(
        code = item.code,
        titleText = truncate(item.title, 40),
        routeCode = item.routeCode,
        approved = "${item.approved}",
        unit = item.unit,
        categoryText = item.category,
        attemptsText = item.attempts.toString(),
        marginText = "%.2f".format(item.margin),
        name = item.name,
        reviewerText = truncate(item.reviewer, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedInspectionBudgetViews(items: MutableList<BatchedInspectionBudget>): MutableList<BatchedInspectionBudgetView> {
    val views = mutableListOf<BatchedInspectionBudgetView>()
    for (item in items) {
        views.add(toBatchedInspectionBudgetView(item))
    }
    return views
}

fun sumBatchedInspectionBudgetView(view: BatchedInspectionBudgetView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedAttempts.toDouble() + parsedMargin
}
