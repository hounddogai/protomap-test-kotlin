package com.acme.transport.fleetops.leasereturn.returnentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled lease return estimate. */
data class ScheduledLeaseReturnEstimateView(
    var codeText: String,
    var label: String,
    var loadKg: String,
    var batchSize: String,
    var expiresAt: String,
    var visible: String,
    var ratioText: String,
    var trailerId: String,
    var lane: String,
    var statusLabel: String,
)

fun toScheduledLeaseReturnEstimateView(item: ScheduledLeaseReturnEstimate): ScheduledLeaseReturnEstimateView {
    return ScheduledLeaseReturnEstimateView(
        codeText = item.code,
        label = item.label,
        loadKg = "%.2f".format(item.loadKg),
        batchSize = item.batchSize.toString(),
        expiresAt = item.expiresAt.toString(),
        visible = "${item.visible}",
        ratioText = "%.2f".format(item.ratio),
        trailerId = item.trailerId,
        lane = truncate(item.lane, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledLeaseReturnEstimateViews(items: MutableList<ScheduledLeaseReturnEstimate>): MutableList<ScheduledLeaseReturnEstimateView> {
    val views = mutableListOf<ScheduledLeaseReturnEstimateView>()
    for (item in items) {
        views.add(toScheduledLeaseReturnEstimateView(item))
    }
    return views
}

fun sumScheduledLeaseReturnEstimateView(view: ScheduledLeaseReturnEstimateView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedLoadKg + parsedBatchSize.toDouble() + parsedExpiresAt.toDouble()
}
