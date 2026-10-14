package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual attempt estimate. */
data class ManualAttemptEstimateView(
    var code: String,
    var label: String,
    var source: String,
    var sequence: String,
    var amountText: String,
    var activeText: String,
    var dockText: String,
    var retries: String,
    var fuelLiters: String,
    var maximum: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toManualAttemptEstimateView(item: ManualAttemptEstimate): ManualAttemptEstimateView {
    return ManualAttemptEstimateView(
        code = item.code,
        label = truncate(item.label, 64),
        source = item.source,
        sequence = item.sequence.toString(),
        amountText = "%.2f".format(item.amount),
        activeText = "${item.active}",
        dockText = item.dock,
        retries = item.retries.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        maximum = "%.2f".format(item.maximum),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualAttemptEstimateViews(items: MutableList<ManualAttemptEstimate>): MutableList<ManualAttemptEstimateView> {
    val views = mutableListOf<ManualAttemptEstimateView>()
    for (item in items) {
        views.add(toManualAttemptEstimateView(item))
    }
    return views
}

fun sumManualAttemptEstimateView(view: ManualAttemptEstimateView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedAmount = view.amountText.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedSequence.toDouble() + parsedAmount + parsedRetries.toDouble()
}
