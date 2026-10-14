package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted dispute snapshot. */
data class WeightedDisputeSnapshotView(
    var code: String,
    var label: String,
    var summary: String,
    var externalRef: String,
    var checksum: String,
    var dock: String,
    var driverId: String,
    var durationMinutes: String,
    var windowDays: String,
    var batchSize: String,
    var channelText: String,
    var statusLabel: String,
)

fun toWeightedDisputeSnapshotView(item: WeightedDisputeSnapshot): WeightedDisputeSnapshotView {
    return WeightedDisputeSnapshotView(
        code = item.code,
        label = truncate(item.label, 64),
        summary = truncate(item.summary, 120),
        externalRef = truncate(item.externalRef, 120),
        checksum = item.checksum,
        dock = truncate(item.dock, 64),
        driverId = item.driverId,
        durationMinutes = item.durationMinutes.toString(),
        windowDays = item.windowDays.toString(),
        batchSize = item.batchSize.toString(),
        channelText = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedDisputeSnapshotViews(items: MutableList<WeightedDisputeSnapshot>): MutableList<WeightedDisputeSnapshotView> {
    val views = mutableListOf<WeightedDisputeSnapshotView>()
    for (item in items) {
        views.add(toWeightedDisputeSnapshotView(item))
    }
    return views
}

fun sumWeightedDisputeSnapshotView(view: WeightedDisputeSnapshotView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedDurationMinutes.toDouble() + parsedWindowDays.toDouble() + parsedBatchSize.toDouble()
}
