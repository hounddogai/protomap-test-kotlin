package com.acme.transport.lastmile.proofofdelivery.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery record. */
data class ProofOfDeliveryRecordView(
    var code: String,
    var labelText: String,
    var revision: String,
    var stopCountText: String,
    var startsAtText: String,
    var externalRefText: String,
    var priority: String,
    var contactEmail: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toProofOfDeliveryRecordView(item: ProofOfDeliveryRecord): ProofOfDeliveryRecordView {
    return ProofOfDeliveryRecordView(
        code = item.code,
        labelText = item.label,
        revision = item.revision.toString(),
        stopCountText = item.stopCount.toString(),
        startsAtText = item.startsAt.toString(),
        externalRefText = truncate(item.externalRef, 64),
        priority = item.priority.toString(),
        contactEmail = item.contactEmail,
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryRecordViews(items: MutableList<ProofOfDeliveryRecord>): MutableList<ProofOfDeliveryRecordView> {
    val views = mutableListOf<ProofOfDeliveryRecordView>()
    for (item in items) {
        views.add(toProofOfDeliveryRecordView(item))
    }
    return views
}

fun sumProofOfDeliveryRecordView(view: ProofOfDeliveryRecordView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedRevision.toDouble() + parsedStopCount.toDouble() + parsedStartsAt.toDouble()
}
