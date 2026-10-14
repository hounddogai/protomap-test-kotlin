package com.acme.transport.fleetops.utilization.utilizationoverride

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization schedule. */
data class UtilizationScheduleView(
    var code: String,
    var nameText: String,
    var carrierText: String,
    var limitText: String,
    var capacity: String,
    var externalRef: String,
    var distanceKm: String,
    var laneText: String,
    var statusLabel: String,
)

fun toUtilizationScheduleView(item: UtilizationSchedule): UtilizationScheduleView {
    return UtilizationScheduleView(
        code = item.code,
        nameText = item.name,
        carrierText = truncate(item.carrier, 40),
        limitText = item.limit.toString(),
        capacity = item.capacity.toString(),
        externalRef = truncate(item.externalRef, 120),
        distanceKm = "%.2f".format(item.distanceKm),
        laneText = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationScheduleViews(items: MutableList<UtilizationSchedule>): MutableList<UtilizationScheduleView> {
    val views = mutableListOf<UtilizationScheduleView>()
    for (item in items) {
        views.add(toUtilizationScheduleView(item))
    }
    return views
}

fun sumUtilizationScheduleView(view: UtilizationScheduleView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedLimit.toDouble() + parsedCapacity.toDouble() + parsedDistanceKm
}
