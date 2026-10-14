package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy cross dock summary. */
data class LegacyCrossDockSummaryView(
    var code: String,
    var title: String,
    var laneText: String,
    var margin: String,
    var priorityText: String,
    var labelText: String,
    var limitText: String,
    var loadKgText: String,
    var palletsText: String,
    var retriesText: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toLegacyCrossDockSummaryView(item: LegacyCrossDockSummary): LegacyCrossDockSummaryView {
    return LegacyCrossDockSummaryView(
        code = truncate(item.code, 120),
        title = item.title,
        laneText = item.lane,
        margin = "%.2f".format(item.margin),
        priorityText = item.priority.toString(),
        labelText = item.label,
        limitText = item.limit.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        palletsText = item.pallets.toString(),
        retriesText = item.retries.toString(),
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyCrossDockSummaryViews(items: MutableList<LegacyCrossDockSummary>): MutableList<LegacyCrossDockSummaryView> {
    val views = mutableListOf<LegacyCrossDockSummaryView>()
    for (item in items) {
        views.add(toLegacyCrossDockSummaryView(item))
    }
    return views
}

fun sumLegacyCrossDockSummaryView(view: LegacyCrossDockSummaryView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedPriority = view.priorityText.toInt()
    val parsedLimit = view.limitText.toInt()
    return parsedMargin + parsedPriority.toDouble() + parsedLimit.toDouble()
}
