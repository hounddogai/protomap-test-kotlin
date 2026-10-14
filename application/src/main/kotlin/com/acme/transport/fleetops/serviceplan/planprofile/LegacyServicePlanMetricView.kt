package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy service plan metric. */
data class LegacyServicePlanMetricView(
    var code: String,
    var label: String,
    var fuelLiters: String,
    var country: String,
    var weightText: String,
    var updatedAtText: String,
    var ratioText: String,
    var titleText: String,
    var carrier: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toLegacyServicePlanMetricView(item: LegacyServicePlanMetric): LegacyServicePlanMetricView {
    return LegacyServicePlanMetricView(
        code = item.code,
        label = item.label,
        fuelLiters = "%.2f".format(item.fuelLiters),
        country = item.country,
        weightText = "%.2f".format(item.weight),
        updatedAtText = item.updatedAt.toString(),
        ratioText = "%.2f".format(item.ratio),
        titleText = truncate(item.title, 80),
        carrier = item.carrier,
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyServicePlanMetricViews(items: MutableList<LegacyServicePlanMetric>): MutableList<LegacyServicePlanMetricView> {
    val views = mutableListOf<LegacyServicePlanMetricView>()
    for (item in items) {
        views.add(toLegacyServicePlanMetricView(item))
    }
    return views
}

fun sumLegacyServicePlanMetricView(view: LegacyServicePlanMetricView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedWeight = view.weightText.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedFuelLiters + parsedWeight + parsedUpdatedAt.toDouble()
}
