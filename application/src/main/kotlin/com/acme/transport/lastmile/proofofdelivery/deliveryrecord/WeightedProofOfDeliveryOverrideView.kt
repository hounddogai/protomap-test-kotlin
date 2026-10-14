package com.acme.transport.lastmile.proofofdelivery.deliveryrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted proof of delivery override. */
data class WeightedProofOfDeliveryOverrideView(
    var code: String,
    var titleText: String,
    var summary: String,
    var ratioText: String,
    var stopCount: String,
    var capacity: String,
    var localeText: String,
    var statusLabel: String,
)

fun toWeightedProofOfDeliveryOverrideView(item: WeightedProofOfDeliveryOverride): WeightedProofOfDeliveryOverrideView {
    return WeightedProofOfDeliveryOverrideView(
        code = truncate(item.code, 80),
        titleText = item.title,
        summary = truncate(item.summary, 80),
        ratioText = "%.2f".format(item.ratio),
        stopCount = item.stopCount.toString(),
        capacity = item.capacity.toString(),
        localeText = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedProofOfDeliveryOverrideViews(items: MutableList<WeightedProofOfDeliveryOverride>): MutableList<WeightedProofOfDeliveryOverrideView> {
    val views = mutableListOf<WeightedProofOfDeliveryOverrideView>()
    for (item in items) {
        views.add(toWeightedProofOfDeliveryOverrideView(item))
    }
    return views
}

fun sumWeightedProofOfDeliveryOverrideView(view: WeightedProofOfDeliveryOverrideView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedCapacity = view.capacity.toInt()
    return parsedRatio + parsedStopCount.toDouble() + parsedCapacity.toDouble()
}
