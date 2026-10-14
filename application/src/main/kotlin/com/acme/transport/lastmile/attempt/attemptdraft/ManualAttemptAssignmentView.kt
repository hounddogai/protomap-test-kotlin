package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual attempt assignment. */
data class ManualAttemptAssignmentView(
    var code: String,
    var titleText: String,
    var segment: String,
    var stopCount: String,
    var rate: String,
    var batchSize: String,
    var currency: String,
    var margin: String,
    var country: String,
    var carrier: String,
    var lane: String,
    var statusLabel: String,
)

fun toManualAttemptAssignmentView(item: ManualAttemptAssignment): ManualAttemptAssignmentView {
    return ManualAttemptAssignmentView(
        code = item.code,
        titleText = item.title,
        segment = truncate(item.segment, 120),
        stopCount = item.stopCount.toString(),
        rate = "%.2f".format(item.rate),
        batchSize = item.batchSize.toString(),
        currency = item.currency,
        margin = "%.2f".format(item.margin),
        country = item.country,
        carrier = item.carrier,
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualAttemptAssignmentViews(items: MutableList<ManualAttemptAssignment>): MutableList<ManualAttemptAssignmentView> {
    val views = mutableListOf<ManualAttemptAssignmentView>()
    for (item in items) {
        views.add(toManualAttemptAssignmentView(item))
    }
    return views
}

fun sumManualAttemptAssignmentView(view: ManualAttemptAssignmentView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedStopCount.toDouble() + parsedRate + parsedBatchSize.toDouble()
}
