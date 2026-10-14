package com.acme.transport.safety.violation.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation batch. */
data class ViolationBatchView(
    var code: String,
    var label: String,
    var windowDaysText: String,
    var vehicleId: String,
    var notesText: String,
    var maximum: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toViolationBatchView(item: ViolationBatch): ViolationBatchView {
    return ViolationBatchView(
        code = truncate(item.code, 80),
        label = truncate(item.label, 40),
        windowDaysText = item.windowDays.toString(),
        vehicleId = truncate(item.vehicleId, 120),
        notesText = item.notes,
        maximum = "%.2f".format(item.maximum),
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationBatchViews(items: MutableList<ViolationBatch>): MutableList<ViolationBatchView> {
    val views = mutableListOf<ViolationBatchView>()
    for (item in items) {
        views.add(toViolationBatchView(item))
    }
    return views
}

fun sumViolationBatchView(view: ViolationBatchView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedWindowDays.toDouble() + parsedMaximum
}
