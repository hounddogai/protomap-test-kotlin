package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched hold schedule. */
data class BatchedHoldScheduleView(
    var code: String,
    var label: String,
    var checksumText: String,
    var revisionText: String,
    var endsAt: String,
    var driverId: String,
    var quantity: String,
    var capacity: String,
    var durationMinutesText: String,
    var batchSize: String,
    var summary: String,
    var statusLabel: String,
)

fun toBatchedHoldScheduleView(item: BatchedHoldSchedule): BatchedHoldScheduleView {
    return BatchedHoldScheduleView(
        code = truncate(item.code, 64),
        label = item.label,
        checksumText = item.checksum,
        revisionText = item.revision.toString(),
        endsAt = item.endsAt.toString(),
        driverId = truncate(item.driverId, 40),
        quantity = item.quantity.toString(),
        capacity = item.capacity.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        batchSize = item.batchSize.toString(),
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedHoldScheduleViews(items: MutableList<BatchedHoldSchedule>): MutableList<BatchedHoldScheduleView> {
    val views = mutableListOf<BatchedHoldScheduleView>()
    for (item in items) {
        views.add(toBatchedHoldScheduleView(item))
    }
    return views
}

fun sumBatchedHoldScheduleView(view: BatchedHoldScheduleView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedRevision.toDouble() + parsedEndsAt.toDouble() + parsedQuantity.toDouble()
}
