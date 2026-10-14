package com.acme.transport.emissions.fuelburn.burnestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted fuel burn estimate. */
data class WeightedFuelBurnEstimateView(
    var codeText: String,
    var name: String,
    var segment: String,
    var depot: String,
    var maximum: String,
    var quantity: String,
    var cost: String,
    var summary: String,
    var fuelLitersText: String,
    var floorValue: String,
    var region: String,
    var statusLabel: String,
)

fun toWeightedFuelBurnEstimateView(item: WeightedFuelBurnEstimate): WeightedFuelBurnEstimateView {
    return WeightedFuelBurnEstimateView(
        codeText = item.code,
        name = item.name,
        segment = item.segment,
        depot = item.depot,
        maximum = "%.2f".format(item.maximum),
        quantity = item.quantity.toString(),
        cost = "%.2f".format(item.cost),
        summary = truncate(item.summary, 80),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        floorValue = "%.2f".format(item.floorValue),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedFuelBurnEstimateViews(items: MutableList<WeightedFuelBurnEstimate>): MutableList<WeightedFuelBurnEstimateView> {
    val views = mutableListOf<WeightedFuelBurnEstimateView>()
    for (item in items) {
        views.add(toWeightedFuelBurnEstimateView(item))
    }
    return views
}

fun sumWeightedFuelBurnEstimateView(view: WeightedFuelBurnEstimateView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedMaximum + parsedQuantity.toDouble() + parsedCost
}
