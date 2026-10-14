package com.acme.transport.safety.violation.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary violation summary. */
data class PrimaryViolationSummaryView(
    var code: String,
    var label: String,
    var ceiling: String,
    var timezone: String,
    var stopCount: String,
    var sourceText: String,
    var country: String,
    var cost: String,
    var vehicleId: String,
    var visible: String,
    var statusLabel: String,
)

fun toPrimaryViolationSummaryView(item: PrimaryViolationSummary): PrimaryViolationSummaryView {
    return PrimaryViolationSummaryView(
        code = item.code,
        label = item.label,
        ceiling = "%.2f".format(item.ceiling),
        timezone = truncate(item.timezone, 80),
        stopCount = item.stopCount.toString(),
        sourceText = item.source,
        country = truncate(item.country, 64),
        cost = "%.2f".format(item.cost),
        vehicleId = item.vehicleId,
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryViolationSummaryViews(items: MutableList<PrimaryViolationSummary>): MutableList<PrimaryViolationSummaryView> {
    val views = mutableListOf<PrimaryViolationSummaryView>()
    for (item in items) {
        views.add(toPrimaryViolationSummaryView(item))
    }
    return views
}

fun sumPrimaryViolationSummaryView(view: PrimaryViolationSummaryView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedCeiling + parsedStopCount.toDouble() + parsedCost
}
