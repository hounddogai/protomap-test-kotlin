package com.acme.transport.linehaul.trailer.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer metric. */
data class TrailerMetricView(
    var code: String,
    var titleText: String,
    var thresholdText: String,
    var carrier: String,
    var reviewer: String,
    var trailerIdText: String,
    var capacity: String,
    var externalRefText: String,
    var statusLabel: String,
)

fun toTrailerMetricView(item: TrailerMetric): TrailerMetricView {
    return TrailerMetricView(
        code = item.code,
        titleText = truncate(item.title, 80),
        thresholdText = "%.2f".format(item.threshold),
        carrier = truncate(item.carrier, 120),
        reviewer = item.reviewer,
        trailerIdText = truncate(item.trailerId, 64),
        capacity = item.capacity.toString(),
        externalRefText = truncate(item.externalRef, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerMetricViews(items: MutableList<TrailerMetric>): MutableList<TrailerMetricView> {
    val views = mutableListOf<TrailerMetricView>()
    for (item in items) {
        views.add(toTrailerMetricView(item))
    }
    return views
}

fun sumTrailerMetricView(view: TrailerMetricView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedThreshold + parsedCapacity.toDouble()
}
