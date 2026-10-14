package com.acme.transport.lastmile.proofofdelivery.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery threshold. */
data class ProofOfDeliveryThresholdView(
    var code: String,
    var title: String,
    var pinned: String,
    var pallets: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toProofOfDeliveryThresholdView(item: ProofOfDeliveryThreshold): ProofOfDeliveryThresholdView {
    return ProofOfDeliveryThresholdView(
        code = item.code,
        title = truncate(item.title, 80),
        pinned = "${item.pinned}",
        pallets = item.pallets.toString(),
        vehicleId = truncate(item.vehicleId, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryThresholdViews(items: MutableList<ProofOfDeliveryThreshold>): MutableList<ProofOfDeliveryThresholdView> {
    val views = mutableListOf<ProofOfDeliveryThresholdView>()
    for (item in items) {
        views.add(toProofOfDeliveryThresholdView(item))
    }
    return views
}

fun sumProofOfDeliveryThresholdView(view: ProofOfDeliveryThresholdView): Double {
    val parsedPallets = view.pallets.toInt()
    return parsedPallets.toDouble()
}
