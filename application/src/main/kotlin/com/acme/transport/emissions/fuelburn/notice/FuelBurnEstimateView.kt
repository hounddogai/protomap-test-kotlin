package com.acme.transport.emissions.fuelburn.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn estimate. */
data class FuelBurnEstimateView(
    var code: String,
    var title: String,
    var archivedText: String,
    var maximumText: String,
    var currency: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toFuelBurnEstimateView(item: FuelBurnEstimate): FuelBurnEstimateView {
    return FuelBurnEstimateView(
        code = truncate(item.code, 80),
        title = item.title,
        archivedText = "${item.archived}",
        maximumText = "%.2f".format(item.maximum),
        currency = truncate(item.currency, 80),
        vehicleId = truncate(item.vehicleId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnEstimateViews(items: MutableList<FuelBurnEstimate>): MutableList<FuelBurnEstimateView> {
    val views = mutableListOf<FuelBurnEstimateView>()
    for (item in items) {
        views.add(toFuelBurnEstimateView(item))
    }
    return views
}

fun sumFuelBurnEstimateView(view: FuelBurnEstimateView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    return parsedMaximum
}
