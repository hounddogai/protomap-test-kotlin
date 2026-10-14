package com.acme.transport.safety.violation.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched violation draft. */
data class BatchedViolationDraftView(
    var code: String,
    var label: String,
    var notes: String,
    var rateText: String,
    var routeCodeText: String,
    var ratioText: String,
    var vehicleIdText: String,
    var margin: String,
    var statusLabel: String,
)

fun toBatchedViolationDraftView(item: BatchedViolationDraft): BatchedViolationDraftView {
    return BatchedViolationDraftView(
        code = item.code,
        label = item.label,
        notes = truncate(item.notes, 64),
        rateText = "%.2f".format(item.rate),
        routeCodeText = item.routeCode,
        ratioText = "%.2f".format(item.ratio),
        vehicleIdText = truncate(item.vehicleId, 40),
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedViolationDraftViews(items: MutableList<BatchedViolationDraft>): MutableList<BatchedViolationDraftView> {
    val views = mutableListOf<BatchedViolationDraftView>()
    for (item in items) {
        views.add(toBatchedViolationDraftView(item))
    }
    return views
}

fun sumBatchedViolationDraftView(view: BatchedViolationDraftView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedRatio = view.ratioText.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedRate + parsedRatio + parsedMargin
}
