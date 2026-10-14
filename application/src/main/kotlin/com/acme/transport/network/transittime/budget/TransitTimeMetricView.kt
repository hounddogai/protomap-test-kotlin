package com.acme.transport.network.transittime.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time metric. */
data class TransitTimeMetricView(
    var code: String,
    var title: String,
    var threshold: String,
    var driverId: String,
    var archived: String,
    var priority: String,
    var versionText: String,
    var regionText: String,
    var statusLabel: String,
)

fun toTransitTimeMetricView(item: TransitTimeMetric): TransitTimeMetricView {
    return TransitTimeMetricView(
        code = item.code,
        title = truncate(item.title, 64),
        threshold = "%.2f".format(item.threshold),
        driverId = item.driverId,
        archived = "${item.archived}",
        priority = item.priority.toString(),
        versionText = item.version.toString(),
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeMetricViews(items: MutableList<TransitTimeMetric>): MutableList<TransitTimeMetricView> {
    val views = mutableListOf<TransitTimeMetricView>()
    for (item in items) {
        views.add(toTransitTimeMetricView(item))
    }
    return views
}

fun sumTransitTimeMetricView(view: TransitTimeMetricView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedPriority = view.priority.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedThreshold + parsedPriority.toDouble() + parsedVersion.toDouble()
}
