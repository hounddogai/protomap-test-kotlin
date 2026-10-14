package com.acme.transport.network.hub.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped hub baseline. */
data class CappedHubBaselineView(
    var codeText: String,
    var label: String,
    var durationMinutes: String,
    var retriesText: String,
    var trailerId: String,
    var batchSize: String,
    var externalRef: String,
    var startsAtText: String,
    var source: String,
    var margin: String,
    var statusLabel: String,
)

fun toCappedHubBaselineView(item: CappedHubBaseline): CappedHubBaselineView {
    return CappedHubBaselineView(
        codeText = item.code,
        label = item.label,
        durationMinutes = item.durationMinutes.toString(),
        retriesText = item.retries.toString(),
        trailerId = item.trailerId,
        batchSize = item.batchSize.toString(),
        externalRef = truncate(item.externalRef, 64),
        startsAtText = item.startsAt.toString(),
        source = item.source,
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedHubBaselineViews(items: MutableList<CappedHubBaseline>): MutableList<CappedHubBaselineView> {
    val views = mutableListOf<CappedHubBaselineView>()
    for (item in items) {
        views.add(toCappedHubBaselineView(item))
    }
    return views
}

fun sumCappedHubBaselineView(view: CappedHubBaselineView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedRetries = view.retriesText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedDurationMinutes.toDouble() + parsedRetries.toDouble() + parsedBatchSize.toDouble()
}
