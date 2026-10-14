package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery checkpoint. */
data class ProofOfDeliveryCheckpointView(
    var code: String,
    var title: String,
    var priority: String,
    var reviewer: String,
    var expiresAtText: String,
    var routeCode: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toProofOfDeliveryCheckpointView(item: ProofOfDeliveryCheckpoint): ProofOfDeliveryCheckpointView {
    return ProofOfDeliveryCheckpointView(
        code = truncate(item.code, 40),
        title = item.title,
        priority = item.priority.toString(),
        reviewer = item.reviewer,
        expiresAtText = item.expiresAt.toString(),
        routeCode = item.routeCode,
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryCheckpointViews(items: MutableList<ProofOfDeliveryCheckpoint>): MutableList<ProofOfDeliveryCheckpointView> {
    val views = mutableListOf<ProofOfDeliveryCheckpointView>()
    for (item in items) {
        views.add(toProofOfDeliveryCheckpointView(item))
    }
    return views
}

fun sumProofOfDeliveryCheckpointView(view: ProofOfDeliveryCheckpointView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedPriority.toDouble() + parsedExpiresAt.toDouble() + parsedDistanceKm
}
