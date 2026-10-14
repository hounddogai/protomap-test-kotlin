package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched attempt override. */
data class BatchedAttemptOverrideView(
    var codeText: String,
    var labelText: String,
    var durationMinutes: String,
    var approved: String,
    var startsAt: String,
    var depotText: String,
    var budget: String,
    var currency: String,
    var priority: String,
    var marginText: String,
    var statusLabel: String,
)

fun toBatchedAttemptOverrideView(item: BatchedAttemptOverride): BatchedAttemptOverrideView {
    return BatchedAttemptOverrideView(
        codeText = item.code,
        labelText = item.label,
        durationMinutes = item.durationMinutes.toString(),
        approved = "${item.approved}",
        startsAt = item.startsAt.toString(),
        depotText = item.depot,
        budget = "%.2f".format(item.budget),
        currency = truncate(item.currency, 80),
        priority = item.priority.toString(),
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedAttemptOverrideViews(items: MutableList<BatchedAttemptOverride>): MutableList<BatchedAttemptOverrideView> {
    val views = mutableListOf<BatchedAttemptOverrideView>()
    for (item in items) {
        views.add(toBatchedAttemptOverrideView(item))
    }
    return views
}

fun sumBatchedAttemptOverrideView(view: BatchedAttemptOverrideView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedDurationMinutes.toDouble() + parsedStartsAt.toDouble() + parsedBudget
}
