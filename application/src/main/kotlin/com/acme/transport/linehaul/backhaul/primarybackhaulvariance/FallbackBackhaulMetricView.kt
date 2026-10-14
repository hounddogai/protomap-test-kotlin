package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback backhaul metric. */
data class FallbackBackhaulMetricView(
    var code: String,
    var label: String,
    var carrierText: String,
    var ceiling: String,
    var weightText: String,
    var amount: String,
    var unit: String,
    var driverId: String,
    var statusLabel: String,
)

fun toFallbackBackhaulMetricView(item: FallbackBackhaulMetric): FallbackBackhaulMetricView {
    return FallbackBackhaulMetricView(
        code = item.code,
        label = truncate(item.label, 64),
        carrierText = item.carrier,
        ceiling = "%.2f".format(item.ceiling),
        weightText = "%.2f".format(item.weight),
        amount = "%.2f".format(item.amount),
        unit = item.unit,
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackBackhaulMetricViews(items: MutableList<FallbackBackhaulMetric>): MutableList<FallbackBackhaulMetricView> {
    val views = mutableListOf<FallbackBackhaulMetricView>()
    for (item in items) {
        views.add(toFallbackBackhaulMetricView(item))
    }
    return views
}

fun sumFallbackBackhaulMetricView(view: FallbackBackhaulMetricView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedWeight = view.weightText.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedCeiling + parsedWeight + parsedAmount
}
