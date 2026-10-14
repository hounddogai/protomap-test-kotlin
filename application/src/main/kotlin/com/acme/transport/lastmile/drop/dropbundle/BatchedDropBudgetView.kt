package com.acme.transport.lastmile.drop.dropbundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched drop budget. */
data class BatchedDropBudgetView(
    var codeText: String,
    var label: String,
    var summaryText: String,
    var unit: String,
    var ownerId: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toBatchedDropBudgetView(item: BatchedDropBudget): BatchedDropBudgetView {
    return BatchedDropBudgetView(
        codeText = item.code,
        label = item.label,
        summaryText = item.summary,
        unit = item.unit,
        ownerId = truncate(item.ownerId, 64),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDropBudgetViews(items: MutableList<BatchedDropBudget>): MutableList<BatchedDropBudgetView> {
    val views = mutableListOf<BatchedDropBudgetView>()
    for (item in items) {
        views.add(toBatchedDropBudgetView(item))
    }
    return views
}

fun sumBatchedDropBudgetView(view: BatchedDropBudgetView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}
