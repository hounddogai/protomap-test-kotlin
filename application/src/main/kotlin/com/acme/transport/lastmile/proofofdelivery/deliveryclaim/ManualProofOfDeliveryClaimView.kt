package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual proof of delivery claim. */
data class ManualProofOfDeliveryClaimView(
    var code: String,
    var labelText: String,
    var distanceKm: String,
    var depot: String,
    var channel: String,
    var retries: String,
    var statusLabel: String,
)

fun toManualProofOfDeliveryClaimView(item: ManualProofOfDeliveryClaim): ManualProofOfDeliveryClaimView {
    return ManualProofOfDeliveryClaimView(
        code = item.code,
        labelText = item.label,
        distanceKm = "%.2f".format(item.distanceKm),
        depot = truncate(item.depot, 40),
        channel = truncate(item.channel, 64),
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualProofOfDeliveryClaimViews(items: MutableList<ManualProofOfDeliveryClaim>): MutableList<ManualProofOfDeliveryClaimView> {
    val views = mutableListOf<ManualProofOfDeliveryClaimView>()
    for (item in items) {
        views.add(toManualProofOfDeliveryClaimView(item))
    }
    return views
}

fun sumManualProofOfDeliveryClaimView(view: ManualProofOfDeliveryClaimView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedDistanceKm + parsedRetries.toDouble()
}
