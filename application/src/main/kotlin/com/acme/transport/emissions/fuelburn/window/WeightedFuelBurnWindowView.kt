package com.acme.transport.emissions.fuelburn.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted fuel burn window. */
data class WeightedFuelBurnWindowView(
    var codeText: String,
    var nameText: String,
    var margin: String,
    var discount: String,
    var weight: String,
    var label: String,
    var active: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toWeightedFuelBurnWindowView(item: WeightedFuelBurnWindow): WeightedFuelBurnWindowView {
    return WeightedFuelBurnWindowView(
        codeText = item.code,
        nameText = item.name,
        margin = "%.2f".format(item.margin),
        discount = "%.2f".format(item.discount),
        weight = "%.2f".format(item.weight),
        label = truncate(item.label, 80),
        active = "${item.active}",
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedFuelBurnWindowViews(items: MutableList<WeightedFuelBurnWindow>): MutableList<WeightedFuelBurnWindowView> {
    val views = mutableListOf<WeightedFuelBurnWindowView>()
    for (item in items) {
        views.add(toWeightedFuelBurnWindowView(item))
    }
    return views
}

fun sumWeightedFuelBurnWindowView(view: WeightedFuelBurnWindowView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedMargin + parsedDiscount + parsedWeight
}
