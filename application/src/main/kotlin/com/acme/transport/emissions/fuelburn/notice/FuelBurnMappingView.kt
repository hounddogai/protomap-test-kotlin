package com.acme.transport.emissions.fuelburn.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn mapping. */
data class FuelBurnMappingView(
    var code: String,
    var titleText: String,
    var pinned: String,
    var name: String,
    var label: String,
    var minimum: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toFuelBurnMappingView(item: FuelBurnMapping): FuelBurnMappingView {
    return FuelBurnMappingView(
        code = item.code,
        titleText = item.title,
        pinned = "${item.pinned}",
        name = item.name,
        label = truncate(item.label, 40),
        minimum = "%.2f".format(item.minimum),
        vehicleId = truncate(item.vehicleId, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnMappingViews(items: MutableList<FuelBurnMapping>): MutableList<FuelBurnMappingView> {
    val views = mutableListOf<FuelBurnMappingView>()
    for (item in items) {
        views.add(toFuelBurnMappingView(item))
    }
    return views
}

fun sumFuelBurnMappingView(view: FuelBurnMappingView): Double {
    val parsedMinimum = view.minimum.toDouble()
    return parsedMinimum
}
