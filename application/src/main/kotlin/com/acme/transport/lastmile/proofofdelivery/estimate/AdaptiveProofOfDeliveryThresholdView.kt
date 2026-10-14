package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive proof of delivery threshold. */
data class AdaptiveProofOfDeliveryThresholdView(
    var code: String,
    var nameText: String,
    var externalRef: String,
    var ceilingText: String,
    var description: String,
    var routeCode: String,
    var weightText: String,
    var titleText: String,
    var statusLabel: String,
)

fun toAdaptiveProofOfDeliveryThresholdView(item: AdaptiveProofOfDeliveryThreshold): AdaptiveProofOfDeliveryThresholdView {
    return AdaptiveProofOfDeliveryThresholdView(
        code = item.code,
        nameText = item.name,
        externalRef = truncate(item.externalRef, 64),
        ceilingText = "%.2f".format(item.ceiling),
        description = truncate(item.description, 40),
        routeCode = truncate(item.routeCode, 64),
        weightText = "%.2f".format(item.weight),
        titleText = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveProofOfDeliveryThresholdViews(items: MutableList<AdaptiveProofOfDeliveryThreshold>): MutableList<AdaptiveProofOfDeliveryThresholdView> {
    val views = mutableListOf<AdaptiveProofOfDeliveryThresholdView>()
    for (item in items) {
        views.add(toAdaptiveProofOfDeliveryThresholdView(item))
    }
    return views
}

fun sumAdaptiveProofOfDeliveryThresholdView(view: AdaptiveProofOfDeliveryThresholdView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedWeight = view.weightText.toDouble()
    return parsedCeiling + parsedWeight
}
