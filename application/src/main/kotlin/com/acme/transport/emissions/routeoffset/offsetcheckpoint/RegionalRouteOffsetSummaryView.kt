package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional route offset summary. */
data class RegionalRouteOffsetSummaryView(
    var codeText: String,
    var label: String,
    var currency: String,
    var capacity: String,
    var margin: String,
    var batchSizeText: String,
    var fuelLitersText: String,
    var graceDays: String,
    var source: String,
    var statusLabel: String,
)

fun toRegionalRouteOffsetSummaryView(item: RegionalRouteOffsetSummary): RegionalRouteOffsetSummaryView {
    return RegionalRouteOffsetSummaryView(
        codeText = truncate(item.code, 80),
        label = truncate(item.label, 64),
        currency = truncate(item.currency, 120),
        capacity = item.capacity.toString(),
        margin = "%.2f".format(item.margin),
        batchSizeText = item.batchSize.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        graceDays = item.graceDays.toString(),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalRouteOffsetSummaryViews(items: MutableList<RegionalRouteOffsetSummary>): MutableList<RegionalRouteOffsetSummaryView> {
    val views = mutableListOf<RegionalRouteOffsetSummaryView>()
    for (item in items) {
        views.add(toRegionalRouteOffsetSummaryView(item))
    }
    return views
}

fun sumRegionalRouteOffsetSummaryView(view: RegionalRouteOffsetSummaryView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedCapacity.toDouble() + parsedMargin + parsedBatchSize.toDouble()
}
