package com.acme.transport.emissions.idling.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional idling summary. */
data class RegionalIdlingSummaryView(
    var code: String,
    var name: String,
    var cost: String,
    var retries: String,
    var thresholdText: String,
    var segmentText: String,
    var endsAt: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toRegionalIdlingSummaryView(item: RegionalIdlingSummary): RegionalIdlingSummaryView {
    return RegionalIdlingSummaryView(
        code = truncate(item.code, 80),
        name = item.name,
        cost = "%.2f".format(item.cost),
        retries = item.retries.toString(),
        thresholdText = "%.2f".format(item.threshold),
        segmentText = item.segment,
        endsAt = item.endsAt.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalIdlingSummaryViews(items: MutableList<RegionalIdlingSummary>): MutableList<RegionalIdlingSummaryView> {
    val views = mutableListOf<RegionalIdlingSummaryView>()
    for (item in items) {
        views.add(toRegionalIdlingSummaryView(item))
    }
    return views
}

fun sumRegionalIdlingSummaryView(view: RegionalIdlingSummaryView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedCost + parsedRetries.toDouble() + parsedThreshold
}
