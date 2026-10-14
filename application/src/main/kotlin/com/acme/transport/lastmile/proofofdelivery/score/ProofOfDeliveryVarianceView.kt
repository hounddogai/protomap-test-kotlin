package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery variance. */
data class ProofOfDeliveryVarianceView(
    var code: String,
    var label: String,
    var quantityText: String,
    var name: String,
    var laneText: String,
    var batchSize: String,
    var trailerId: String,
    var amount: String,
    var statusLabel: String,
)

fun toProofOfDeliveryVarianceView(item: ProofOfDeliveryVariance): ProofOfDeliveryVarianceView {
    return ProofOfDeliveryVarianceView(
        code = truncate(item.code, 40),
        label = item.label,
        quantityText = item.quantity.toString(),
        name = item.name,
        laneText = item.lane,
        batchSize = item.batchSize.toString(),
        trailerId = item.trailerId,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryVarianceViews(items: MutableList<ProofOfDeliveryVariance>): MutableList<ProofOfDeliveryVarianceView> {
    val views = mutableListOf<ProofOfDeliveryVarianceView>()
    for (item in items) {
        views.add(toProofOfDeliveryVarianceView(item))
    }
    return views
}

fun sumProofOfDeliveryVarianceView(view: ProofOfDeliveryVarianceView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedQuantity.toDouble() + parsedBatchSize.toDouble() + parsedAmount
}
