package com.acme.transport.lastmile.proofofdelivery.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery baseline. */
data class ProofOfDeliveryBaselineView(
    var code: String,
    var label: String,
    var weight: String,
    var windowDays: String,
    var carrierText: String,
    var statusLabel: String,
)

fun toProofOfDeliveryBaselineView(item: ProofOfDeliveryBaseline): ProofOfDeliveryBaselineView {
    return ProofOfDeliveryBaselineView(
        code = item.code,
        label = item.label,
        weight = "%.2f".format(item.weight),
        windowDays = item.windowDays.toString(),
        carrierText = truncate(item.carrier, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryBaselineViews(items: MutableList<ProofOfDeliveryBaseline>): MutableList<ProofOfDeliveryBaselineView> {
    val views = mutableListOf<ProofOfDeliveryBaselineView>()
    for (item in items) {
        views.add(toProofOfDeliveryBaselineView(item))
    }
    return views
}

fun sumProofOfDeliveryBaselineView(view: ProofOfDeliveryBaselineView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedWeight + parsedWindowDays.toDouble()
}
