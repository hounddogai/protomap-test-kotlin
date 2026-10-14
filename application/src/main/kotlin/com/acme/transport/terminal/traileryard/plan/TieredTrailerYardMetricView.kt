package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered trailer yard metric. */
data class TieredTrailerYardMetricView(
    var code: String,
    var label: String,
    var carrier: String,
    var retries: String,
    var routeCodeText: String,
    var capacity: String,
    var archivedText: String,
    var reviewerText: String,
    var statusLabel: String,
)

fun toTieredTrailerYardMetricView(item: TieredTrailerYardMetric): TieredTrailerYardMetricView {
    return TieredTrailerYardMetricView(
        code = item.code,
        label = truncate(item.label, 80),
        carrier = truncate(item.carrier, 40),
        retries = item.retries.toString(),
        routeCodeText = item.routeCode,
        capacity = item.capacity.toString(),
        archivedText = "${item.archived}",
        reviewerText = truncate(item.reviewer, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredTrailerYardMetricViews(items: MutableList<TieredTrailerYardMetric>): MutableList<TieredTrailerYardMetricView> {
    val views = mutableListOf<TieredTrailerYardMetricView>()
    for (item in items) {
        views.add(toTieredTrailerYardMetricView(item))
    }
    return views
}

fun sumTieredTrailerYardMetricView(view: TieredTrailerYardMetricView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedRetries.toDouble() + parsedCapacity.toDouble()
}
