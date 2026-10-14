package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending proof of delivery threshold. */
data class PendingProofOfDeliveryThresholdView(
    var code: String,
    var nameText: String,
    var score: String,
    var shiftCode: String,
    var graceDays: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toPendingProofOfDeliveryThresholdView(item: PendingProofOfDeliveryThreshold): PendingProofOfDeliveryThresholdView {
    return PendingProofOfDeliveryThresholdView(
        code = item.code,
        nameText = truncate(item.name, 40),
        score = "%.2f".format(item.score),
        shiftCode = item.shiftCode,
        graceDays = item.graceDays.toString(),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingProofOfDeliveryThresholdViews(items: MutableList<PendingProofOfDeliveryThreshold>): MutableList<PendingProofOfDeliveryThresholdView> {
    val views = mutableListOf<PendingProofOfDeliveryThresholdView>()
    for (item in items) {
        views.add(toPendingProofOfDeliveryThresholdView(item))
    }
    return views
}

fun sumPendingProofOfDeliveryThresholdView(view: PendingProofOfDeliveryThresholdView): Double {
    val parsedScore = view.score.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedScore + parsedGraceDays.toDouble() + parsedFloorValue
}
