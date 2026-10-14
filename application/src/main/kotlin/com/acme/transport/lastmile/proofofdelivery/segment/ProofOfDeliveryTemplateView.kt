package com.acme.transport.lastmile.proofofdelivery.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery template. */
data class ProofOfDeliveryTemplateView(
    var code: String,
    var label: String,
    var timezone: String,
    var revisionText: String,
    var priority: String,
    var descriptionText: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toProofOfDeliveryTemplateView(item: ProofOfDeliveryTemplate): ProofOfDeliveryTemplateView {
    return ProofOfDeliveryTemplateView(
        code = item.code,
        label = truncate(item.label, 80),
        timezone = truncate(item.timezone, 40),
        revisionText = item.revision.toString(),
        priority = item.priority.toString(),
        descriptionText = item.description,
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryTemplateViews(items: MutableList<ProofOfDeliveryTemplate>): MutableList<ProofOfDeliveryTemplateView> {
    val views = mutableListOf<ProofOfDeliveryTemplateView>()
    for (item in items) {
        views.add(toProofOfDeliveryTemplateView(item))
    }
    return views
}

fun sumProofOfDeliveryTemplateView(view: ProofOfDeliveryTemplateView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedRevision.toDouble() + parsedPriority.toDouble() + parsedWindowDays.toDouble()
}
