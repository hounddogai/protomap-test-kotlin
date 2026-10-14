package com.acme.transport.emissions.idling.idlingwindow

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default idling metric. */
data class DefaultIdlingMetricView(
    var code: String,
    var title: String,
    var distanceKmText: String,
    var rate: String,
    var carrier: String,
    var revisionText: String,
    var limit: String,
    var version: String,
    var etaMinutesText: String,
    var quantity: String,
    var capacity: String,
    var statusLabel: String,
)

fun toDefaultIdlingMetricView(item: DefaultIdlingMetric): DefaultIdlingMetricView {
    return DefaultIdlingMetricView(
        code = item.code,
        title = item.title,
        distanceKmText = "%.2f".format(item.distanceKm),
        rate = "%.2f".format(item.rate),
        carrier = truncate(item.carrier, 80),
        revisionText = item.revision.toString(),
        limit = item.limit.toString(),
        version = item.version.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        quantity = item.quantity.toString(),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultIdlingMetricViews(items: MutableList<DefaultIdlingMetric>): MutableList<DefaultIdlingMetricView> {
    val views = mutableListOf<DefaultIdlingMetricView>()
    for (item in items) {
        views.add(toDefaultIdlingMetricView(item))
    }
    return views
}

fun sumDefaultIdlingMetricView(view: DefaultIdlingMetricView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedRevision = view.revisionText.toInt()
    return parsedDistanceKm + parsedRate + parsedRevision.toDouble()
}
