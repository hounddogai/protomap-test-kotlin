package com.acme.transport.customs.clearance.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance metric. */
data class ClearanceMetricView(
    var codeText: String,
    var title: String,
    var revisionText: String,
    var threshold: String,
    var stopCountText: String,
    var vehicleId: String,
    var reviewer: String,
    var summary: String,
    var statusLabel: String,
)

fun toClearanceMetricView(item: ClearanceMetric): ClearanceMetricView {
    return ClearanceMetricView(
        codeText = item.code,
        title = item.title,
        revisionText = item.revision.toString(),
        threshold = "%.2f".format(item.threshold),
        stopCountText = item.stopCount.toString(),
        vehicleId = item.vehicleId,
        reviewer = truncate(item.reviewer, 40),
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceMetricViews(items: MutableList<ClearanceMetric>): MutableList<ClearanceMetricView> {
    val views = mutableListOf<ClearanceMetricView>()
    for (item in items) {
        views.add(toClearanceMetricView(item))
    }
    return views
}

fun sumClearanceMetricView(view: ClearanceMetricView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedRevision.toDouble() + parsedThreshold + parsedStopCount.toDouble()
}
