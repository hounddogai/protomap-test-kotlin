package com.acme.transport.network.hub.hubsummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled hub variance. */
data class ScheduledHubVarianceView(
    var code: String,
    var label: String,
    var unit: String,
    var sequenceText: String,
    var routeCode: String,
    var laneText: String,
    var floorValue: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toScheduledHubVarianceView(item: ScheduledHubVariance): ScheduledHubVarianceView {
    return ScheduledHubVarianceView(
        code = truncate(item.code, 80),
        label = item.label,
        unit = item.unit,
        sequenceText = item.sequence.toString(),
        routeCode = item.routeCode,
        laneText = item.lane,
        floorValue = "%.2f".format(item.floorValue),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledHubVarianceViews(items: MutableList<ScheduledHubVariance>): MutableList<ScheduledHubVarianceView> {
    val views = mutableListOf<ScheduledHubVarianceView>()
    for (item in items) {
        views.add(toScheduledHubVarianceView(item))
    }
    return views
}

fun sumScheduledHubVarianceView(view: ScheduledHubVarianceView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedSequence.toDouble() + parsedFloorValue + parsedDistanceKm
}
