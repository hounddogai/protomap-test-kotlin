package com.acme.transport.lastmile.proofofdelivery.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped proof of delivery group. */
data class CappedProofOfDeliveryGroupView(
    var codeText: String,
    var label: String,
    var unit: String,
    var sequence: String,
    var createdAtText: String,
    var trailerIdText: String,
    var updatedAt: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toCappedProofOfDeliveryGroupView(item: CappedProofOfDeliveryGroup): CappedProofOfDeliveryGroupView {
    return CappedProofOfDeliveryGroupView(
        codeText = item.code,
        label = item.label,
        unit = truncate(item.unit, 40),
        sequence = item.sequence.toString(),
        createdAtText = item.createdAt.toString(),
        trailerIdText = item.trailerId,
        updatedAt = item.updatedAt.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedProofOfDeliveryGroupViews(items: MutableList<CappedProofOfDeliveryGroup>): MutableList<CappedProofOfDeliveryGroupView> {
    val views = mutableListOf<CappedProofOfDeliveryGroupView>()
    for (item in items) {
        views.add(toCappedProofOfDeliveryGroupView(item))
    }
    return views
}

fun sumCappedProofOfDeliveryGroupView(view: CappedProofOfDeliveryGroupView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedSequence.toDouble() + parsedCreatedAt.toDouble() + parsedUpdatedAt.toDouble()
}
