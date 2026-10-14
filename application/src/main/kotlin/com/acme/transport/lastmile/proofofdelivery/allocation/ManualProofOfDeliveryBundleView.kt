package com.acme.transport.lastmile.proofofdelivery.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual proof of delivery bundle. */
data class ManualProofOfDeliveryBundleView(
    var codeText: String,
    var label: String,
    var externalRef: String,
    var carrier: String,
    var trailerId: String,
    var maximum: String,
    var weight: String,
    var statusLabel: String,
)

fun toManualProofOfDeliveryBundleView(item: ManualProofOfDeliveryBundle): ManualProofOfDeliveryBundleView {
    return ManualProofOfDeliveryBundleView(
        codeText = item.code,
        label = item.label,
        externalRef = truncate(item.externalRef, 120),
        carrier = item.carrier,
        trailerId = truncate(item.trailerId, 40),
        maximum = "%.2f".format(item.maximum),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualProofOfDeliveryBundleViews(items: MutableList<ManualProofOfDeliveryBundle>): MutableList<ManualProofOfDeliveryBundleView> {
    val views = mutableListOf<ManualProofOfDeliveryBundleView>()
    for (item in items) {
        views.add(toManualProofOfDeliveryBundleView(item))
    }
    return views
}

fun sumManualProofOfDeliveryBundleView(view: ManualProofOfDeliveryBundleView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedMaximum + parsedWeight
}
