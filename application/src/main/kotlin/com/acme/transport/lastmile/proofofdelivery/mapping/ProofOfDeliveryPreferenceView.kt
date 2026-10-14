package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery preference. */
data class ProofOfDeliveryPreferenceView(
    var code: String,
    var label: String,
    var source: String,
    var minimum: String,
    var routeCodeText: String,
    var statusLabel: String,
)

fun toProofOfDeliveryPreferenceView(item: ProofOfDeliveryPreference): ProofOfDeliveryPreferenceView {
    return ProofOfDeliveryPreferenceView(
        code = item.code,
        label = truncate(item.label, 40),
        source = item.source,
        minimum = "%.2f".format(item.minimum),
        routeCodeText = truncate(item.routeCode, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryPreferenceViews(items: MutableList<ProofOfDeliveryPreference>): MutableList<ProofOfDeliveryPreferenceView> {
    val views = mutableListOf<ProofOfDeliveryPreferenceView>()
    for (item in items) {
        views.add(toProofOfDeliveryPreferenceView(item))
    }
    return views
}

fun sumProofOfDeliveryPreferenceView(view: ProofOfDeliveryPreferenceView): Double {
    val parsedMinimum = view.minimum.toDouble()
    return parsedMinimum
}
