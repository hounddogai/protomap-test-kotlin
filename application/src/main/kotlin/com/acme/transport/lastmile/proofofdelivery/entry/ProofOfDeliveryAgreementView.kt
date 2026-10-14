package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery agreement. */
data class ProofOfDeliveryAgreementView(
    var code: String,
    var nameText: String,
    var revision: String,
    var expiresAt: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toProofOfDeliveryAgreementView(item: ProofOfDeliveryAgreement): ProofOfDeliveryAgreementView {
    return ProofOfDeliveryAgreementView(
        code = truncate(item.code, 120),
        nameText = item.name,
        revision = item.revision.toString(),
        expiresAt = item.expiresAt.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryAgreementViews(items: MutableList<ProofOfDeliveryAgreement>): MutableList<ProofOfDeliveryAgreementView> {
    val views = mutableListOf<ProofOfDeliveryAgreementView>()
    for (item in items) {
        views.add(toProofOfDeliveryAgreementView(item))
    }
    return views
}

fun sumProofOfDeliveryAgreementView(view: ProofOfDeliveryAgreementView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedRevision.toDouble() + parsedExpiresAt.toDouble() + parsedWindowDays.toDouble()
}
