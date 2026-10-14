package com.acme.transport.freightpay.dispute.disputegroup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary dispute checkpoint. */
data class PrimaryDisputeCheckpointView(
    var codeText: String,
    var label: String,
    var distanceKm: String,
    var discountText: String,
    var dockText: String,
    var attemptsText: String,
    var trailerId: String,
    var notes: String,
    var expiresAt: String,
    var country: String,
    var statusLabel: String,
)

fun toPrimaryDisputeCheckpointView(item: PrimaryDisputeCheckpoint): PrimaryDisputeCheckpointView {
    return PrimaryDisputeCheckpointView(
        codeText = item.code,
        label = truncate(item.label, 120),
        distanceKm = "%.2f".format(item.distanceKm),
        discountText = "%.2f".format(item.discount),
        dockText = item.dock,
        attemptsText = item.attempts.toString(),
        trailerId = item.trailerId,
        notes = item.notes,
        expiresAt = item.expiresAt.toString(),
        country = truncate(item.country, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryDisputeCheckpointViews(items: MutableList<PrimaryDisputeCheckpoint>): MutableList<PrimaryDisputeCheckpointView> {
    val views = mutableListOf<PrimaryDisputeCheckpointView>()
    for (item in items) {
        views.add(toPrimaryDisputeCheckpointView(item))
    }
    return views
}

fun sumPrimaryDisputeCheckpointView(view: PrimaryDisputeCheckpointView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedDistanceKm + parsedDiscount + parsedAttempts.toDouble()
}
