package com.acme.transport.emissions.routeoffset.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted route offset target. */
data class WeightedRouteOffsetTargetView(
    var code: String,
    var title: String,
    var versionText: String,
    var expiresAt: String,
    var odometer: String,
    var summaryText: String,
    var weightText: String,
    var unitText: String,
    var score: String,
    var lane: String,
    var statusLabel: String,
)

fun toWeightedRouteOffsetTargetView(item: WeightedRouteOffsetTarget): WeightedRouteOffsetTargetView {
    return WeightedRouteOffsetTargetView(
        code = truncate(item.code, 64),
        title = truncate(item.title, 80),
        versionText = item.version.toString(),
        expiresAt = item.expiresAt.toString(),
        odometer = item.odometer.toString(),
        summaryText = item.summary,
        weightText = "%.2f".format(item.weight),
        unitText = truncate(item.unit, 40),
        score = "%.2f".format(item.score),
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedRouteOffsetTargetViews(items: MutableList<WeightedRouteOffsetTarget>): MutableList<WeightedRouteOffsetTargetView> {
    val views = mutableListOf<WeightedRouteOffsetTargetView>()
    for (item in items) {
        views.add(toWeightedRouteOffsetTargetView(item))
    }
    return views
}

fun sumWeightedRouteOffsetTargetView(view: WeightedRouteOffsetTargetView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedVersion.toDouble() + parsedExpiresAt.toDouble() + parsedOdometer.toDouble()
}
