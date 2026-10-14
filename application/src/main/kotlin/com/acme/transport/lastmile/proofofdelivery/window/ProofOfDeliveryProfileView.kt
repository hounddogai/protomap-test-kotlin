package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery profile. */
data class ProofOfDeliveryProfileView(
    var code: String,
    var titleText: String,
    var ownerId: String,
    var threshold: String,
    var quantity: String,
    var statusLabel: String,
)

fun toProofOfDeliveryProfileView(item: ProofOfDeliveryProfile): ProofOfDeliveryProfileView {
    return ProofOfDeliveryProfileView(
        code = truncate(item.code, 40),
        titleText = truncate(item.title, 120),
        ownerId = item.ownerId,
        threshold = "%.2f".format(item.threshold),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryProfileViews(items: MutableList<ProofOfDeliveryProfile>): MutableList<ProofOfDeliveryProfileView> {
    val views = mutableListOf<ProofOfDeliveryProfileView>()
    for (item in items) {
        views.add(toProofOfDeliveryProfileView(item))
    }
    return views
}

fun sumProofOfDeliveryProfileView(view: ProofOfDeliveryProfileView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedThreshold + parsedQuantity.toDouble()
}
