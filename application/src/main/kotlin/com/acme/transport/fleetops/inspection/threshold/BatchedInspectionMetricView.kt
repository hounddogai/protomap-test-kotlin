package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched inspection metric. */
data class BatchedInspectionMetricView(
    var codeText: String,
    var titleText: String,
    var summary: String,
    var lane: String,
    var marginText: String,
    var statusLabel: String,
)

fun toBatchedInspectionMetricView(item: BatchedInspectionMetric): BatchedInspectionMetricView {
    return BatchedInspectionMetricView(
        codeText = item.code,
        titleText = truncate(item.title, 80),
        summary = item.summary,
        lane = truncate(item.lane, 40),
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedInspectionMetricViews(items: MutableList<BatchedInspectionMetric>): MutableList<BatchedInspectionMetricView> {
    val views = mutableListOf<BatchedInspectionMetricView>()
    for (item in items) {
        views.add(toBatchedInspectionMetricView(item))
    }
    return views
}

fun sumBatchedInspectionMetricView(view: BatchedInspectionMetricView): Double {
    val parsedMargin = view.marginText.toDouble()
    return parsedMargin
}
