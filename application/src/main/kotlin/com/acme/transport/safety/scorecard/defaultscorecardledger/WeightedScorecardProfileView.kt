package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted scorecard profile. */
data class WeightedScorecardProfileView(
    var codeText: String,
    var label: String,
    var unit: String,
    var quantity: String,
    var retries: String,
    var weightText: String,
    var region: String,
    var etaMinutes: String,
    var margin: String,
    var statusLabel: String,
)

fun toWeightedScorecardProfileView(item: WeightedScorecardProfile): WeightedScorecardProfileView {
    return WeightedScorecardProfileView(
        codeText = item.code,
        label = truncate(item.label, 64),
        unit = item.unit,
        quantity = item.quantity.toString(),
        retries = item.retries.toString(),
        weightText = "%.2f".format(item.weight),
        region = item.region,
        etaMinutes = item.etaMinutes.toString(),
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedScorecardProfileViews(items: MutableList<WeightedScorecardProfile>): MutableList<WeightedScorecardProfileView> {
    val views = mutableListOf<WeightedScorecardProfileView>()
    for (item in items) {
        views.add(toWeightedScorecardProfileView(item))
    }
    return views
}

fun sumWeightedScorecardProfileView(view: WeightedScorecardProfileView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedQuantity.toDouble() + parsedRetries.toDouble() + parsedWeight
}
