package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched qualification bundle. */
data class BatchedQualificationBundleView(
    var code: String,
    var label: String,
    var routeCodeText: String,
    var description: String,
    var weight: String,
    var margin: String,
    var regionText: String,
    var loadKgText: String,
    var etaMinutesText: String,
    var minimumText: String,
    var currency: String,
    var statusLabel: String,
)

fun toBatchedQualificationBundleView(item: BatchedQualificationBundle): BatchedQualificationBundleView {
    return BatchedQualificationBundleView(
        code = item.code,
        label = item.label,
        routeCodeText = truncate(item.routeCode, 40),
        description = item.description,
        weight = "%.2f".format(item.weight),
        margin = "%.2f".format(item.margin),
        regionText = item.region,
        loadKgText = "%.2f".format(item.loadKg),
        etaMinutesText = item.etaMinutes.toString(),
        minimumText = "%.2f".format(item.minimum),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedQualificationBundleViews(items: MutableList<BatchedQualificationBundle>): MutableList<BatchedQualificationBundleView> {
    val views = mutableListOf<BatchedQualificationBundleView>()
    for (item in items) {
        views.add(toBatchedQualificationBundleView(item))
    }
    return views
}

fun sumBatchedQualificationBundleView(view: BatchedQualificationBundleView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedWeight + parsedMargin + parsedLoadKg
}
