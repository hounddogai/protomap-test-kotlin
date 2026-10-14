package com.acme.transport.crewing.availability.scheduledavailabilitytier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched availability tier. */
data class BatchedAvailabilityTierView(
    var codeText: String,
    var name: String,
    var durationMinutes: String,
    var segmentText: String,
    var retries: String,
    var currency: String,
    var regionText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toBatchedAvailabilityTierView(item: BatchedAvailabilityTier): BatchedAvailabilityTierView {
    return BatchedAvailabilityTierView(
        codeText = item.code,
        name = item.name,
        durationMinutes = item.durationMinutes.toString(),
        segmentText = item.segment,
        retries = item.retries.toString(),
        currency = truncate(item.currency, 40),
        regionText = truncate(item.region, 120),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedAvailabilityTierViews(items: MutableList<BatchedAvailabilityTier>): MutableList<BatchedAvailabilityTierView> {
    val views = mutableListOf<BatchedAvailabilityTierView>()
    for (item in items) {
        views.add(toBatchedAvailabilityTierView(item))
    }
    return views
}

fun sumBatchedAvailabilityTierView(view: BatchedAvailabilityTierView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedDurationMinutes.toDouble() + parsedRetries.toDouble()
}
