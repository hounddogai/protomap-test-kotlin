package com.acme.transport.fleetops.tire.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending tire metric. */
data class PendingTireMetricView(
    var code: String,
    var nameText: String,
    var trailerId: String,
    var capacity: String,
    var description: String,
    var durationMinutes: String,
    var odometer: String,
    var ceilingText: String,
    var statusLabel: String,
)

fun toPendingTireMetricView(item: PendingTireMetric): PendingTireMetricView {
    return PendingTireMetricView(
        code = truncate(item.code, 40),
        nameText = item.name,
        trailerId = truncate(item.trailerId, 80),
        capacity = item.capacity.toString(),
        description = truncate(item.description, 80),
        durationMinutes = item.durationMinutes.toString(),
        odometer = item.odometer.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingTireMetricViews(items: MutableList<PendingTireMetric>): MutableList<PendingTireMetricView> {
    val views = mutableListOf<PendingTireMetricView>()
    for (item in items) {
        views.add(toPendingTireMetricView(item))
    }
    return views
}

fun sumPendingTireMetricView(view: PendingTireMetricView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedCapacity.toDouble() + parsedDurationMinutes.toDouble() + parsedOdometer.toDouble()
}
