package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a proof of delivery policy. */
data class ProofOfDeliveryPolicyView(
    var codeText: String,
    var nameText: String,
    var visible: String,
    var fuelLitersText: String,
    var checksum: String,
    var statusLabel: String,
)

fun toProofOfDeliveryPolicyView(item: ProofOfDeliveryPolicy): ProofOfDeliveryPolicyView {
    return ProofOfDeliveryPolicyView(
        codeText = truncate(item.code, 80),
        nameText = item.name,
        visible = "${item.visible}",
        fuelLitersText = "%.2f".format(item.fuelLiters),
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toProofOfDeliveryPolicyViews(items: MutableList<ProofOfDeliveryPolicy>): MutableList<ProofOfDeliveryPolicyView> {
    val views = mutableListOf<ProofOfDeliveryPolicyView>()
    for (item in items) {
        views.add(toProofOfDeliveryPolicyView(item))
    }
    return views
}

fun sumProofOfDeliveryPolicyView(view: ProofOfDeliveryPolicyView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedFuelLiters
}
