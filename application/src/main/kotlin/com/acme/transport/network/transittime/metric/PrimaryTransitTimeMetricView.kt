package com.acme.transport.network.transittime.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary transit time metric. */
data class PrimaryTransitTimeMetricView(
    var code: String,
    var name: String,
    var trailerId: String,
    var stopCount: String,
    var countryText: String,
    var statusLabel: String,
)

fun toPrimaryTransitTimeMetricView(item: PrimaryTransitTimeMetric): PrimaryTransitTimeMetricView {
    return PrimaryTransitTimeMetricView(
        code = truncate(item.code, 80),
        name = item.name,
        trailerId = item.trailerId,
        stopCount = item.stopCount.toString(),
        countryText = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTransitTimeMetricViews(items: MutableList<PrimaryTransitTimeMetric>): MutableList<PrimaryTransitTimeMetricView> {
    val views = mutableListOf<PrimaryTransitTimeMetricView>()
    for (item in items) {
        views.add(toPrimaryTransitTimeMetricView(item))
    }
    return views
}

fun sumPrimaryTransitTimeMetricView(view: PrimaryTransitTimeMetricView): Double {
    val parsedStopCount = view.stopCount.toInt()
    return parsedStopCount.toDouble()
}
