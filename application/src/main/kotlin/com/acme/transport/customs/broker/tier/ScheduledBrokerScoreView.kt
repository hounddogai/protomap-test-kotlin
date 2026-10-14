package com.acme.transport.customs.broker.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled broker score. */
data class ScheduledBrokerScoreView(
    var codeText: String,
    var label: String,
    var externalRefText: String,
    var capacityText: String,
    var shiftCode: String,
    var trailerId: String,
    var unit: String,
    var driverId: String,
    var regionText: String,
    var stopCountText: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toScheduledBrokerScoreView(item: ScheduledBrokerScore): ScheduledBrokerScoreView {
    return ScheduledBrokerScoreView(
        codeText = item.code,
        label = truncate(item.label, 64),
        externalRefText = item.externalRef,
        capacityText = item.capacity.toString(),
        shiftCode = truncate(item.shiftCode, 120),
        trailerId = item.trailerId,
        unit = item.unit,
        driverId = item.driverId,
        regionText = item.region,
        stopCountText = item.stopCount.toString(),
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledBrokerScoreViews(items: MutableList<ScheduledBrokerScore>): MutableList<ScheduledBrokerScoreView> {
    val views = mutableListOf<ScheduledBrokerScoreView>()
    for (item in items) {
        views.add(toScheduledBrokerScoreView(item))
    }
    return views
}

fun sumScheduledBrokerScoreView(view: ScheduledBrokerScoreView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedRatio = view.ratioText.toDouble()
    return parsedCapacity.toDouble() + parsedStopCount.toDouble() + parsedRatio
}
