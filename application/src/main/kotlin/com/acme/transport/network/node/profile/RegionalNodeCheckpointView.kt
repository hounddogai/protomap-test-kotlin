package com.acme.transport.network.node.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional node checkpoint. */
data class RegionalNodeCheckpointView(
    var codeText: String,
    var title: String,
    var ratio: String,
    var trailerId: String,
    var locale: String,
    var durationMinutes: String,
    var budget: String,
    var statusLabel: String,
)

fun toRegionalNodeCheckpointView(item: RegionalNodeCheckpoint): RegionalNodeCheckpointView {
    return RegionalNodeCheckpointView(
        codeText = truncate(item.code, 120),
        title = item.title,
        ratio = "%.2f".format(item.ratio),
        trailerId = item.trailerId,
        locale = item.locale,
        durationMinutes = item.durationMinutes.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalNodeCheckpointViews(items: MutableList<RegionalNodeCheckpoint>): MutableList<RegionalNodeCheckpointView> {
    val views = mutableListOf<RegionalNodeCheckpointView>()
    for (item in items) {
        views.add(toRegionalNodeCheckpointView(item))
    }
    return views
}

fun sumRegionalNodeCheckpointView(view: RegionalNodeCheckpointView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedRatio + parsedDurationMinutes.toDouble() + parsedBudget
}
