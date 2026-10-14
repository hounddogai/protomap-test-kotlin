package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling rate card segment. */
data class RollingRateCardSegmentView(
    var code: String,
    var titleText: String,
    var distanceKmText: String,
    var trailerIdText: String,
    var thresholdText: String,
    var segment: String,
    var cost: String,
    var pinned: String,
    var statusLabel: String,
)

fun toRollingRateCardSegmentView(item: RollingRateCardSegment): RollingRateCardSegmentView {
    return RollingRateCardSegmentView(
        code = item.code,
        titleText = truncate(item.title, 120),
        distanceKmText = "%.2f".format(item.distanceKm),
        trailerIdText = item.trailerId,
        thresholdText = "%.2f".format(item.threshold),
        segment = item.segment,
        cost = "%.2f".format(item.cost),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingRateCardSegmentViews(items: MutableList<RollingRateCardSegment>): MutableList<RollingRateCardSegmentView> {
    val views = mutableListOf<RollingRateCardSegmentView>()
    for (item in items) {
        views.add(toRollingRateCardSegmentView(item))
    }
    return views
}

fun sumRollingRateCardSegmentView(view: RollingRateCardSegmentView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedDistanceKm + parsedThreshold + parsedCost
}
