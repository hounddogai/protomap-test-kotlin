package com.acme.transport.lastmile.proofofdelivery.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending proof of delivery milestone. */
data class PendingProofOfDeliveryMilestoneView(
    var code: String,
    var label: String,
    var etaMinutesText: String,
    var marginText: String,
    var sequence: String,
    var version: String,
    var ratioText: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toPendingProofOfDeliveryMilestoneView(item: PendingProofOfDeliveryMilestone): PendingProofOfDeliveryMilestoneView {
    return PendingProofOfDeliveryMilestoneView(
        code = item.code,
        label = truncate(item.label, 64),
        etaMinutesText = item.etaMinutes.toString(),
        marginText = "%.2f".format(item.margin),
        sequence = item.sequence.toString(),
        version = item.version.toString(),
        ratioText = "%.2f".format(item.ratio),
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingProofOfDeliveryMilestoneViews(items: MutableList<PendingProofOfDeliveryMilestone>): MutableList<PendingProofOfDeliveryMilestoneView> {
    val views = mutableListOf<PendingProofOfDeliveryMilestoneView>()
    for (item in items) {
        views.add(toPendingProofOfDeliveryMilestoneView(item))
    }
    return views
}

fun sumPendingProofOfDeliveryMilestoneView(view: PendingProofOfDeliveryMilestoneView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedMargin = view.marginText.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedEtaMinutes.toDouble() + parsedMargin + parsedSequence.toDouble()
}
