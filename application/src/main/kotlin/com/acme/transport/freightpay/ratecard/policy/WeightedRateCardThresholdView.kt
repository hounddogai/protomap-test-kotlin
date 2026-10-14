package com.acme.transport.freightpay.ratecard.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted rate card threshold. */
data class WeightedRateCardThresholdView(
    var codeText: String,
    var name: String,
    var odometer: String,
    var createdAtText: String,
    var versionText: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toWeightedRateCardThresholdView(item: WeightedRateCardThreshold): WeightedRateCardThresholdView {
    return WeightedRateCardThresholdView(
        codeText = item.code,
        name = truncate(item.name, 80),
        odometer = item.odometer.toString(),
        createdAtText = item.createdAt.toString(),
        versionText = item.version.toString(),
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedRateCardThresholdViews(items: MutableList<WeightedRateCardThreshold>): MutableList<WeightedRateCardThresholdView> {
    val views = mutableListOf<WeightedRateCardThresholdView>()
    for (item in items) {
        views.add(toWeightedRateCardThresholdView(item))
    }
    return views
}

fun sumWeightedRateCardThresholdView(view: WeightedRateCardThresholdView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedOdometer.toDouble() + parsedCreatedAt.toDouble() + parsedVersion.toDouble()
}
