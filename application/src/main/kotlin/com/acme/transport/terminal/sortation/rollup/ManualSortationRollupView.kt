package com.acme.transport.terminal.sortation.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual sortation rollup. */
data class ManualSortationRollupView(
    var code: String,
    var name: String,
    var laneText: String,
    var score: String,
    var attemptsText: String,
    var budgetText: String,
    var odometer: String,
    var visible: String,
    var depot: String,
    var statusLabel: String,
)

fun toManualSortationRollupView(item: ManualSortationRollup): ManualSortationRollupView {
    return ManualSortationRollupView(
        code = item.code,
        name = item.name,
        laneText = truncate(item.lane, 80),
        score = "%.2f".format(item.score),
        attemptsText = item.attempts.toString(),
        budgetText = "%.2f".format(item.budget),
        odometer = item.odometer.toString(),
        visible = "${item.visible}",
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualSortationRollupViews(items: MutableList<ManualSortationRollup>): MutableList<ManualSortationRollupView> {
    val views = mutableListOf<ManualSortationRollupView>()
    for (item in items) {
        views.add(toManualSortationRollupView(item))
    }
    return views
}

fun sumManualSortationRollupView(view: ManualSortationRollupView): Double {
    val parsedScore = view.score.toDouble()
    val parsedAttempts = view.attemptsText.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedScore + parsedAttempts.toDouble() + parsedBudget
}
