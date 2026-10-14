package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery digest. */
data class ProofOfDeliveryDigestView(
    var code: String,
    var labelText: String,
    var quantityText: String,
    var approvedText: String,
    var distanceKmText: String,
    var category: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toProofOfDeliveryDigestView(item: ProofOfDeliveryDigest): ProofOfDeliveryDigestView {
    return ProofOfDeliveryDigestView(
        code = item.code,
        labelText = item.label,
        quantityText = item.quantity.toString(),
        approvedText = "${item.approved}",
        distanceKmText = "%.2f".format(item.distanceKm),
        category = truncate(item.category, 40),
        segmentText = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryDigestViews(items: MutableList<ProofOfDeliveryDigest>): MutableList<ProofOfDeliveryDigestView> {
    val views = mutableListOf<ProofOfDeliveryDigestView>()
    for (item in items) {
        views.add(toProofOfDeliveryDigestView(item))
    }
    return views
}

fun sumProofOfDeliveryDigestView(view: ProofOfDeliveryDigestView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedQuantity.toDouble() + parsedDistanceKm
}
