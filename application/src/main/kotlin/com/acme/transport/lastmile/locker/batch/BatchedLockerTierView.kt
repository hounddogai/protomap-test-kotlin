package com.acme.transport.lastmile.locker.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched locker tier. */
data class BatchedLockerTierView(
    var codeText: String,
    var labelText: String,
    var createdAt: String,
    var contactEmail: String,
    var routeCode: String,
    var rate: String,
    var vehicleId: String,
    var notes: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toBatchedLockerTierView(item: BatchedLockerTier): BatchedLockerTierView {
    return BatchedLockerTierView(
        codeText = item.code,
        labelText = truncate(item.label, 64),
        createdAt = item.createdAt.toString(),
        contactEmail = item.contactEmail,
        routeCode = item.routeCode,
        rate = "%.2f".format(item.rate),
        vehicleId = truncate(item.vehicleId, 80),
        notes = truncate(item.notes, 80),
        segmentText = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedLockerTierViews(items: MutableList<BatchedLockerTier>): MutableList<BatchedLockerTierView> {
    val views = mutableListOf<BatchedLockerTierView>()
    for (item in items) {
        views.add(toBatchedLockerTierView(item))
    }
    return views
}

fun sumBatchedLockerTierView(view: BatchedLockerTierView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedCreatedAt.toDouble() + parsedRate
}
