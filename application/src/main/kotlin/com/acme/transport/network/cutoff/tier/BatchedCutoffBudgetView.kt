package com.acme.transport.network.cutoff.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched cutoff budget. */
data class BatchedCutoffBudgetView(
    var code: String,
    var title: String,
    var contactEmail: String,
    var locale: String,
    var stopCountText: String,
    var label: String,
    var pinned: String,
    var statusLabel: String,
)

fun toBatchedCutoffBudgetView(item: BatchedCutoffBudget): BatchedCutoffBudgetView {
    return BatchedCutoffBudgetView(
        code = item.code,
        title = truncate(item.title, 120),
        contactEmail = item.contactEmail,
        locale = item.locale,
        stopCountText = item.stopCount.toString(),
        label = truncate(item.label, 80),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedCutoffBudgetViews(items: MutableList<BatchedCutoffBudget>): MutableList<BatchedCutoffBudgetView> {
    val views = mutableListOf<BatchedCutoffBudgetView>()
    for (item in items) {
        views.add(toBatchedCutoffBudgetView(item))
    }
    return views
}

fun sumBatchedCutoffBudgetView(view: BatchedCutoffBudgetView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    return parsedStopCount.toDouble()
}
