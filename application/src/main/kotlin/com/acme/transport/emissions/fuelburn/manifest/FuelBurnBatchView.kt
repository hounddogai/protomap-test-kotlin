package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn batch. */
data class FuelBurnBatchView(
    var code: String,
    var name: String,
    var costText: String,
    var currency: String,
    var threshold: String,
    var floorValueText: String,
    var depot: String,
    var fuelLiters: String,
    var loadKgText: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toFuelBurnBatchView(item: FuelBurnBatch): FuelBurnBatchView {
    return FuelBurnBatchView(
        code = item.code,
        name = truncate(item.name, 64),
        costText = "%.2f".format(item.cost),
        currency = item.currency,
        threshold = "%.2f".format(item.threshold),
        floorValueText = "%.2f".format(item.floorValue),
        depot = item.depot,
        fuelLiters = "%.2f".format(item.fuelLiters),
        loadKgText = "%.2f".format(item.loadKg),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnBatchViews(items: MutableList<FuelBurnBatch>): MutableList<FuelBurnBatchView> {
    val views = mutableListOf<FuelBurnBatchView>()
    for (item in items) {
        views.add(toFuelBurnBatchView(item))
    }
    return views
}

fun sumFuelBurnBatchView(view: FuelBurnBatchView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedCost + parsedThreshold + parsedFloorValue
}
