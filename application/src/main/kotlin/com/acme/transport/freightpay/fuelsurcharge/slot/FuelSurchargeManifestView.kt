package com.acme.transport.freightpay.fuelsurcharge.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge manifest. */
data class FuelSurchargeManifestView(
    var code: String,
    var label: String,
    var odometerText: String,
    var stopCount: String,
    var weight: String,
    var ratioText: String,
    var rate: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toFuelSurchargeManifestView(item: FuelSurchargeManifest): FuelSurchargeManifestView {
    return FuelSurchargeManifestView(
        code = item.code,
        label = truncate(item.label, 120),
        odometerText = item.odometer.toString(),
        stopCount = item.stopCount.toString(),
        weight = "%.2f".format(item.weight),
        ratioText = "%.2f".format(item.ratio),
        rate = "%.2f".format(item.rate),
        vehicleId = truncate(item.vehicleId, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeManifestViews(items: MutableList<FuelSurchargeManifest>): MutableList<FuelSurchargeManifestView> {
    val views = mutableListOf<FuelSurchargeManifestView>()
    for (item in items) {
        views.add(toFuelSurchargeManifestView(item))
    }
    return views
}

fun sumFuelSurchargeManifestView(view: FuelSurchargeManifestView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedOdometer.toDouble() + parsedStopCount.toDouble() + parsedWeight
}
