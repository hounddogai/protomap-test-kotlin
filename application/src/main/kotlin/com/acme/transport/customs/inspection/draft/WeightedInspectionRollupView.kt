package com.acme.transport.customs.inspection.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted inspection rollup. */
data class WeightedInspectionRollupView(
    var code: String,
    var title: String,
    var odometerText: String,
    var distanceKm: String,
    var weight: String,
    var ceilingText: String,
    var locale: String,
    var retriesText: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toWeightedInspectionRollupView(item: WeightedInspectionRollup): WeightedInspectionRollupView {
    return WeightedInspectionRollupView(
        code = item.code,
        title = item.title,
        odometerText = item.odometer.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        weight = "%.2f".format(item.weight),
        ceilingText = "%.2f".format(item.ceiling),
        locale = truncate(item.locale, 80),
        retriesText = item.retries.toString(),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedInspectionRollupViews(items: MutableList<WeightedInspectionRollup>): MutableList<WeightedInspectionRollupView> {
    val views = mutableListOf<WeightedInspectionRollupView>()
    for (item in items) {
        views.add(toWeightedInspectionRollupView(item))
    }
    return views
}

fun sumWeightedInspectionRollupView(view: WeightedInspectionRollupView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedOdometer.toDouble() + parsedDistanceKm + parsedWeight
}
