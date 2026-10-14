package com.acme.transport.network.transittime.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time allocation. */
data class TransitTimeAllocationView(
    var code: String,
    var name: String,
    var sequence: String,
    var score: String,
    var retries: String,
    var cost: String,
    var vehicleIdText: String,
    var limit: String,
    var margin: String,
    var timezoneText: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toTransitTimeAllocationView(item: TransitTimeAllocation): TransitTimeAllocationView {
    return TransitTimeAllocationView(
        code = truncate(item.code, 120),
        name = item.name,
        sequence = item.sequence.toString(),
        score = "%.2f".format(item.score),
        retries = item.retries.toString(),
        cost = "%.2f".format(item.cost),
        vehicleIdText = truncate(item.vehicleId, 64),
        limit = item.limit.toString(),
        margin = "%.2f".format(item.margin),
        timezoneText = item.timezone,
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeAllocationViews(items: MutableList<TransitTimeAllocation>): MutableList<TransitTimeAllocationView> {
    val views = mutableListOf<TransitTimeAllocationView>()
    for (item in items) {
        views.add(toTransitTimeAllocationView(item))
    }
    return views
}

fun sumTransitTimeAllocationView(view: TransitTimeAllocationView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedScore = view.score.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedSequence.toDouble() + parsedScore + parsedRetries.toDouble()
}
