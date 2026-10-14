package com.acme.transport.safety.scorecard.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard budget. */
data class ScorecardBudgetView(
    var code: String,
    var name: String,
    var batchSize: String,
    var ownerId: String,
    var maximum: String,
    var segment: String,
    var dockText: String,
    var pinned: String,
    var driverIdText: String,
    var statusLabel: String,
)

fun toScorecardBudgetView(item: ScorecardBudget): ScorecardBudgetView {
    return ScorecardBudgetView(
        code = truncate(item.code, 80),
        name = item.name,
        batchSize = item.batchSize.toString(),
        ownerId = truncate(item.ownerId, 40),
        maximum = "%.2f".format(item.maximum),
        segment = truncate(item.segment, 40),
        dockText = item.dock,
        pinned = "${item.pinned}",
        driverIdText = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardBudgetViews(items: MutableList<ScorecardBudget>): MutableList<ScorecardBudgetView> {
    val views = mutableListOf<ScorecardBudgetView>()
    for (item in items) {
        views.add(toScorecardBudgetView(item))
    }
    return views
}

fun sumScorecardBudgetView(view: ScorecardBudgetView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedBatchSize.toDouble() + parsedMaximum
}
