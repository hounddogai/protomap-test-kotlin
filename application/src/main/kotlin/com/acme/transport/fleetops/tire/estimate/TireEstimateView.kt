package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire estimate. */
data class TireEstimateView(
    var code: String,
    var label: String,
    var description: String,
    var trailerId: String,
    var expiresAtText: String,
    var sourceText: String,
    var weight: String,
    var maximum: String,
    var statusLabel: String,
)

fun toTireEstimateView(item: TireEstimate): TireEstimateView {
    return TireEstimateView(
        code = item.code,
        label = item.label,
        description = truncate(item.description, 64),
        trailerId = item.trailerId,
        expiresAtText = item.expiresAt.toString(),
        sourceText = item.source,
        weight = "%.2f".format(item.weight),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireEstimateViews(items: MutableList<TireEstimate>): MutableList<TireEstimateView> {
    val views = mutableListOf<TireEstimateView>()
    for (item in items) {
        views.add(toTireEstimateView(item))
    }
    return views
}

fun sumTireEstimateView(view: TireEstimateView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedExpiresAt.toDouble() + parsedWeight + parsedMaximum
}
