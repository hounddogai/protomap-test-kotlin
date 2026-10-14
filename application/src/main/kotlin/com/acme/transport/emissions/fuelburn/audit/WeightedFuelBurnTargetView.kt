package com.acme.transport.emissions.fuelburn.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted fuel burn target. */
data class WeightedFuelBurnTargetView(
    var code: String,
    var nameText: String,
    var ceilingText: String,
    var depotText: String,
    var odometer: String,
    var createdAt: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toWeightedFuelBurnTargetView(item: WeightedFuelBurnTarget): WeightedFuelBurnTargetView {
    return WeightedFuelBurnTargetView(
        code = item.code,
        nameText = item.name,
        ceilingText = "%.2f".format(item.ceiling),
        depotText = item.depot,
        odometer = item.odometer.toString(),
        createdAt = item.createdAt.toString(),
        shiftCodeText = truncate(item.shiftCode, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedFuelBurnTargetViews(items: MutableList<WeightedFuelBurnTarget>): MutableList<WeightedFuelBurnTargetView> {
    val views = mutableListOf<WeightedFuelBurnTargetView>()
    for (item in items) {
        views.add(toWeightedFuelBurnTargetView(item))
    }
    return views
}

fun sumWeightedFuelBurnTargetView(view: WeightedFuelBurnTargetView): Double {
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedOdometer = view.odometer.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedCeiling + parsedOdometer.toDouble() + parsedCreatedAt.toDouble()
}
