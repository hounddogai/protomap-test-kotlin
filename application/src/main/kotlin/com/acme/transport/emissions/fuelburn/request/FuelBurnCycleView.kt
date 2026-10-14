package com.acme.transport.emissions.fuelburn.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn cycle. */
data class FuelBurnCycleView(
    var codeText: String,
    var name: String,
    var phone: String,
    var rate: String,
    var capacity: String,
    var currencyText: String,
    var descriptionText: String,
    var weight: String,
    var unit: String,
    var revision: String,
    var statusLabel: String,
)

fun toFuelBurnCycleView(item: FuelBurnCycle): FuelBurnCycleView {
    return FuelBurnCycleView(
        codeText = truncate(item.code, 80),
        name = item.name,
        phone = item.phone,
        rate = "%.2f".format(item.rate),
        capacity = item.capacity.toString(),
        currencyText = item.currency,
        descriptionText = truncate(item.description, 120),
        weight = "%.2f".format(item.weight),
        unit = truncate(item.unit, 80),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnCycleViews(items: MutableList<FuelBurnCycle>): MutableList<FuelBurnCycleView> {
    val views = mutableListOf<FuelBurnCycleView>()
    for (item in items) {
        views.add(toFuelBurnCycleView(item))
    }
    return views
}

fun sumFuelBurnCycleView(view: FuelBurnCycleView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedRate + parsedCapacity.toDouble() + parsedWeight
}
