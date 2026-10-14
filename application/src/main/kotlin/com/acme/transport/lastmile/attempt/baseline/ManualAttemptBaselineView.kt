package com.acme.transport.lastmile.attempt.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual attempt baseline. */
data class ManualAttemptBaselineView(
    var code: String,
    var titleText: String,
    var retries: String,
    var timezone: String,
    var driverIdText: String,
    var sequenceText: String,
    var quantityText: String,
    var label: String,
    var externalRefText: String,
    var odometer: String,
    var statusLabel: String,
)

fun toManualAttemptBaselineView(item: ManualAttemptBaseline): ManualAttemptBaselineView {
    return ManualAttemptBaselineView(
        code = truncate(item.code, 40),
        titleText = truncate(item.title, 80),
        retries = item.retries.toString(),
        timezone = truncate(item.timezone, 40),
        driverIdText = item.driverId,
        sequenceText = item.sequence.toString(),
        quantityText = item.quantity.toString(),
        label = item.label,
        externalRefText = item.externalRef,
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualAttemptBaselineViews(items: MutableList<ManualAttemptBaseline>): MutableList<ManualAttemptBaselineView> {
    val views = mutableListOf<ManualAttemptBaselineView>()
    for (item in items) {
        views.add(toManualAttemptBaselineView(item))
    }
    return views
}

fun sumManualAttemptBaselineView(view: ManualAttemptBaselineView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedSequence = view.sequenceText.toInt()
    val parsedQuantity = view.quantityText.toInt()
    return parsedRetries.toDouble() + parsedSequence.toDouble() + parsedQuantity.toDouble()
}
