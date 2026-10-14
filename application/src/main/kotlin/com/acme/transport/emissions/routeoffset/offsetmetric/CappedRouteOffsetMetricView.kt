package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped route offset metric. */
data class CappedRouteOffsetMetricView(
    var code: String,
    var name: String,
    var revisionText: String,
    var titleText: String,
    var loadKg: String,
    var unit: String,
    var approvedText: String,
    var batchSize: String,
    var segmentText: String,
    var durationMinutesText: String,
    var amount: String,
    var statusLabel: String,
)

fun toCappedRouteOffsetMetricView(item: CappedRouteOffsetMetric): CappedRouteOffsetMetricView {
    return CappedRouteOffsetMetricView(
        code = item.code,
        name = truncate(item.name, 40),
        revisionText = item.revision.toString(),
        titleText = item.title,
        loadKg = "%.2f".format(item.loadKg),
        unit = item.unit,
        approvedText = "${item.approved}",
        batchSize = item.batchSize.toString(),
        segmentText = truncate(item.segment, 64),
        durationMinutesText = item.durationMinutes.toString(),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedRouteOffsetMetricViews(items: MutableList<CappedRouteOffsetMetric>): MutableList<CappedRouteOffsetMetricView> {
    val views = mutableListOf<CappedRouteOffsetMetricView>()
    for (item in items) {
        views.add(toCappedRouteOffsetMetricView(item))
    }
    return views
}

fun sumCappedRouteOffsetMetricView(view: CappedRouteOffsetMetricView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedRevision.toDouble() + parsedLoadKg + parsedBatchSize.toDouble()
}
