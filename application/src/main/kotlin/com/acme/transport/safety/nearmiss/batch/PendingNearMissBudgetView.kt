package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending near miss budget. */
data class PendingNearMissBudgetView(
    var code: String,
    var title: String,
    var graceDays: String,
    var expiresAtText: String,
    var routeCode: String,
    var trailerIdText: String,
    var budget: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toPendingNearMissBudgetView(item: PendingNearMissBudget): PendingNearMissBudgetView {
    return PendingNearMissBudgetView(
        code = item.code,
        title = truncate(item.title, 64),
        graceDays = item.graceDays.toString(),
        expiresAtText = item.expiresAt.toString(),
        routeCode = item.routeCode,
        trailerIdText = item.trailerId,
        budget = "%.2f".format(item.budget),
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingNearMissBudgetViews(items: MutableList<PendingNearMissBudget>): MutableList<PendingNearMissBudgetView> {
    val views = mutableListOf<PendingNearMissBudgetView>()
    for (item in items) {
        views.add(toPendingNearMissBudgetView(item))
    }
    return views
}

fun sumPendingNearMissBudgetView(view: PendingNearMissBudgetView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedGraceDays.toDouble() + parsedExpiresAt.toDouble() + parsedBudget
}
