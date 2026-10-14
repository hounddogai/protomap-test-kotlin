package com.acme.transport.emissions.routeoffset.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset summary. */
data class RouteOffsetSummaryView(
    var code: String,
    var title: String,
    var stopCount: String,
    var driverId: String,
    var batchSizeText: String,
    var reviewer: String,
    var sequence: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toRouteOffsetSummaryView(item: RouteOffsetSummary): RouteOffsetSummaryView {
    return RouteOffsetSummaryView(
        code = item.code,
        title = truncate(item.title, 80),
        stopCount = item.stopCount.toString(),
        driverId = truncate(item.driverId, 64),
        batchSizeText = item.batchSize.toString(),
        reviewer = truncate(item.reviewer, 64),
        sequence = item.sequence.toString(),
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetSummaryViews(items: MutableList<RouteOffsetSummary>): MutableList<RouteOffsetSummaryView> {
    val views = mutableListOf<RouteOffsetSummaryView>()
    for (item in items) {
        views.add(toRouteOffsetSummaryView(item))
    }
    return views
}

fun sumRouteOffsetSummaryView(view: RouteOffsetSummaryView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedStopCount.toDouble() + parsedBatchSize.toDouble() + parsedSequence.toDouble()
}
