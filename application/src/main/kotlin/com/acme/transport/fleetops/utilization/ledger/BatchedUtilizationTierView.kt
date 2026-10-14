package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched utilization tier. */
data class BatchedUtilizationTierView(
    var code: String,
    var labelText: String,
    var score: String,
    var active: String,
    var distanceKmText: String,
    var driverId: String,
    var startsAtText: String,
    var stopCountText: String,
    var carrier: String,
    var shiftCode: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toBatchedUtilizationTierView(item: BatchedUtilizationTier): BatchedUtilizationTierView {
    return BatchedUtilizationTierView(
        code = truncate(item.code, 120),
        labelText = item.label,
        score = "%.2f".format(item.score),
        active = "${item.active}",
        distanceKmText = "%.2f".format(item.distanceKm),
        driverId = item.driverId,
        startsAtText = item.startsAt.toString(),
        stopCountText = item.stopCount.toString(),
        carrier = item.carrier,
        shiftCode = item.shiftCode,
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedUtilizationTierViews(items: MutableList<BatchedUtilizationTier>): MutableList<BatchedUtilizationTierView> {
    val views = mutableListOf<BatchedUtilizationTierView>()
    for (item in items) {
        views.add(toBatchedUtilizationTierView(item))
    }
    return views
}

fun sumBatchedUtilizationTierView(view: BatchedUtilizationTierView): Double {
    val parsedScore = view.score.toDouble()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedScore + parsedDistanceKm + parsedStartsAt.toDouble()
}
