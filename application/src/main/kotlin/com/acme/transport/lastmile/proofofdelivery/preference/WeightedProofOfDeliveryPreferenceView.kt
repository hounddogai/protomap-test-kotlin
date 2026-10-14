package com.acme.transport.lastmile.proofofdelivery.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted proof of delivery preference. */
data class WeightedProofOfDeliveryPreferenceView(
    var code: String,
    var title: String,
    var phoneText: String,
    var createdAt: String,
    var sourceText: String,
    var margin: String,
    var depot: String,
    var active: String,
    var statusLabel: String,
)

fun toWeightedProofOfDeliveryPreferenceView(item: WeightedProofOfDeliveryPreference): WeightedProofOfDeliveryPreferenceView {
    return WeightedProofOfDeliveryPreferenceView(
        code = item.code,
        title = item.title,
        phoneText = item.phone,
        createdAt = item.createdAt.toString(),
        sourceText = truncate(item.source, 40),
        margin = "%.2f".format(item.margin),
        depot = item.depot,
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedProofOfDeliveryPreferenceViews(items: MutableList<WeightedProofOfDeliveryPreference>): MutableList<WeightedProofOfDeliveryPreferenceView> {
    val views = mutableListOf<WeightedProofOfDeliveryPreferenceView>()
    for (item in items) {
        views.add(toWeightedProofOfDeliveryPreferenceView(item))
    }
    return views
}

fun sumWeightedProofOfDeliveryPreferenceView(view: WeightedProofOfDeliveryPreferenceView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedCreatedAt.toDouble() + parsedMargin
}
