package com.acme.transport.crewing.roster.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster metric. */
data class RosterMetricView(
    var code: String,
    var name: String,
    var thresholdText: String,
    var limitText: String,
    var distanceKm: String,
    var capacity: String,
    var descriptionText: String,
    var attempts: String,
    var sequence: String,
    var statusLabel: String,
)

fun toRosterMetricView(item: RosterMetric): RosterMetricView {
    return RosterMetricView(
        code = item.code,
        name = truncate(item.name, 64),
        thresholdText = "%.2f".format(item.threshold),
        limitText = item.limit.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        capacity = item.capacity.toString(),
        descriptionText = item.description,
        attempts = item.attempts.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterMetricViews(items: MutableList<RosterMetric>): MutableList<RosterMetricView> {
    val views = mutableListOf<RosterMetricView>()
    for (item in items) {
        views.add(toRosterMetricView(item))
    }
    return views
}

fun sumRosterMetricView(view: RosterMetricView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedLimit = view.limitText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedThreshold + parsedLimit.toDouble() + parsedDistanceKm
}
