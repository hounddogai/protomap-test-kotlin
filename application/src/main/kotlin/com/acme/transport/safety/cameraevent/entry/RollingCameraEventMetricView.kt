package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling camera event metric. */
data class RollingCameraEventMetricView(
    var code: String,
    var nameText: String,
    var driverId: String,
    var reviewer: String,
    var title: String,
    var lane: String,
    var attempts: String,
    var trailerId: String,
    var quantity: String,
    var statusLabel: String,
)

fun toRollingCameraEventMetricView(item: RollingCameraEventMetric): RollingCameraEventMetricView {
    return RollingCameraEventMetricView(
        code = item.code,
        nameText = truncate(item.name, 120),
        driverId = item.driverId,
        reviewer = item.reviewer,
        title = truncate(item.title, 40),
        lane = item.lane,
        attempts = item.attempts.toString(),
        trailerId = truncate(item.trailerId, 64),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingCameraEventMetricViews(items: MutableList<RollingCameraEventMetric>): MutableList<RollingCameraEventMetricView> {
    val views = mutableListOf<RollingCameraEventMetricView>()
    for (item in items) {
        views.add(toRollingCameraEventMetricView(item))
    }
    return views
}

fun sumRollingCameraEventMetricView(view: RollingCameraEventMetricView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedAttempts.toDouble() + parsedQuantity.toDouble()
}
