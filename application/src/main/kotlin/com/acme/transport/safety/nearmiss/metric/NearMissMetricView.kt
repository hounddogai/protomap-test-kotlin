package com.acme.transport.safety.nearmiss.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss metric. */
data class NearMissMetricView(
    var codeText: String,
    var label: String,
    var trailerId: String,
    var laneText: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toNearMissMetricView(item: NearMissMetric): NearMissMetricView {
    return NearMissMetricView(
        codeText = item.code,
        label = item.label,
        trailerId = truncate(item.trailerId, 64),
        laneText = truncate(item.lane, 80),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissMetricViews(items: MutableList<NearMissMetric>): MutableList<NearMissMetricView> {
    val views = mutableListOf<NearMissMetricView>()
    for (item in items) {
        views.add(toNearMissMetricView(item))
    }
    return views
}

fun sumNearMissMetricView(view: NearMissMetricView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedFuelLiters
}
