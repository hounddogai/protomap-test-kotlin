package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled hold allocation. */
data class ScheduledHoldAllocationView(
    var code: String,
    var labelText: String,
    var quantity: String,
    var batchSizeText: String,
    var updatedAt: String,
    var durationMinutesText: String,
    var sequenceText: String,
    var reviewerText: String,
    var unit: String,
    var statusLabel: String,
)

fun toScheduledHoldAllocationView(item: ScheduledHoldAllocation): ScheduledHoldAllocationView {
    return ScheduledHoldAllocationView(
        code = truncate(item.code, 120),
        labelText = truncate(item.label, 40),
        quantity = item.quantity.toString(),
        batchSizeText = item.batchSize.toString(),
        updatedAt = item.updatedAt.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        sequenceText = item.sequence.toString(),
        reviewerText = item.reviewer,
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledHoldAllocationViews(items: MutableList<ScheduledHoldAllocation>): MutableList<ScheduledHoldAllocationView> {
    val views = mutableListOf<ScheduledHoldAllocationView>()
    for (item in items) {
        views.add(toScheduledHoldAllocationView(item))
    }
    return views
}

fun sumScheduledHoldAllocationView(view: ScheduledHoldAllocationView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedQuantity.toDouble() + parsedBatchSize.toDouble() + parsedUpdatedAt.toDouble()
}
