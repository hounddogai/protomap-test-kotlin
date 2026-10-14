package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery segment. */
data class ProofOfDeliverySegmentView(
    var code: String,
    var nameText: String,
    var channel: String,
    var notesText: String,
    var ownerIdText: String,
    var amount: String,
    var statusLabel: String,
)

fun toProofOfDeliverySegmentView(item: ProofOfDeliverySegment): ProofOfDeliverySegmentView {
    return ProofOfDeliverySegmentView(
        code = item.code,
        nameText = truncate(item.name, 80),
        channel = item.channel,
        notesText = item.notes,
        ownerIdText = truncate(item.ownerId, 80),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliverySegmentViews(items: MutableList<ProofOfDeliverySegment>): MutableList<ProofOfDeliverySegmentView> {
    val views = mutableListOf<ProofOfDeliverySegmentView>()
    for (item in items) {
        views.add(toProofOfDeliverySegmentView(item))
    }
    return views
}

fun sumProofOfDeliverySegmentView(view: ProofOfDeliverySegmentView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
