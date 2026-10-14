package com.acme.transport.fleetops.utilization.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization budget. */
data class UtilizationBudgetView(
    var code: String,
    var name: String,
    var categoryText: String,
    var minimumText: String,
    var scoreText: String,
    var unitText: String,
    var stopCount: String,
    var etaMinutesText: String,
    var amount: String,
    var pinned: String,
    var checksum: String,
    var statusLabel: String,
)

fun toUtilizationBudgetView(item: UtilizationBudget): UtilizationBudgetView {
    return UtilizationBudgetView(
        code = item.code,
        name = item.name,
        categoryText = item.category,
        minimumText = "%.2f".format(item.minimum),
        scoreText = "%.2f".format(item.score),
        unitText = truncate(item.unit, 120),
        stopCount = item.stopCount.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        amount = "%.2f".format(item.amount),
        pinned = "${item.pinned}",
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationBudgetViews(items: MutableList<UtilizationBudget>): MutableList<UtilizationBudgetView> {
    val views = mutableListOf<UtilizationBudgetView>()
    for (item in items) {
        views.add(toUtilizationBudgetView(item))
    }
    return views
}

fun sumUtilizationBudgetView(view: UtilizationBudgetView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedScore = view.scoreText.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedMinimum + parsedScore + parsedStopCount.toDouble()
}
