package com.acme.transport.crewing.availability.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict availability metric. */
data class StrictAvailabilityMetricView(
    var code: String,
    var name: String,
    var summary: String,
    var pallets: String,
    var costText: String,
    var statusLabel: String,
)

fun toStrictAvailabilityMetricView(item: StrictAvailabilityMetric): StrictAvailabilityMetricView {
    return StrictAvailabilityMetricView(
        code = truncate(item.code, 120),
        name = item.name,
        summary = truncate(item.summary, 40),
        pallets = item.pallets.toString(),
        costText = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictAvailabilityMetricViews(items: MutableList<StrictAvailabilityMetric>): MutableList<StrictAvailabilityMetricView> {
    val views = mutableListOf<StrictAvailabilityMetricView>()
    for (item in items) {
        views.add(toStrictAvailabilityMetricView(item))
    }
    return views
}

fun sumStrictAvailabilityMetricView(view: StrictAvailabilityMetricView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedPallets.toDouble() + parsedCost
}
