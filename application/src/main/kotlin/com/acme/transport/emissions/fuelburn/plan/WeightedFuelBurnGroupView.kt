package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted fuel burn group. */
data class WeightedFuelBurnGroupView(
    var code: String,
    var labelText: String,
    var depotText: String,
    var distanceKmText: String,
    var palletsText: String,
    var notes: String,
    var vehicleId: String,
    var retries: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toWeightedFuelBurnGroupView(item: WeightedFuelBurnGroup): WeightedFuelBurnGroupView {
    return WeightedFuelBurnGroupView(
        code = truncate(item.code, 64),
        labelText = item.label,
        depotText = truncate(item.depot, 80),
        distanceKmText = "%.2f".format(item.distanceKm),
        palletsText = item.pallets.toString(),
        notes = item.notes,
        vehicleId = item.vehicleId,
        retries = item.retries.toString(),
        shiftCodeText = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedFuelBurnGroupViews(items: MutableList<WeightedFuelBurnGroup>): MutableList<WeightedFuelBurnGroupView> {
    val views = mutableListOf<WeightedFuelBurnGroupView>()
    for (item in items) {
        views.add(toWeightedFuelBurnGroupView(item))
    }
    return views
}

fun sumWeightedFuelBurnGroupView(view: WeightedFuelBurnGroupView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedPallets = view.palletsText.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedDistanceKm + parsedPallets.toDouble() + parsedRetries.toDouble()
}
