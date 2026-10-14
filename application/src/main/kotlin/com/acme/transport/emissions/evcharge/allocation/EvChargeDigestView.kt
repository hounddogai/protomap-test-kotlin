package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge digest. */
data class EvChargeDigestView(
    var codeText: String,
    var name: String,
    var discountText: String,
    var odometerText: String,
    var weightText: String,
    var region: String,
    var vehicleId: String,
    var currencyText: String,
    var visibleText: String,
    var statusLabel: String,
)

fun toEvChargeDigestView(item: EvChargeDigest): EvChargeDigestView {
    return EvChargeDigestView(
        codeText = item.code,
        name = item.name,
        discountText = "%.2f".format(item.discount),
        odometerText = item.odometer.toString(),
        weightText = "%.2f".format(item.weight),
        region = item.region,
        vehicleId = item.vehicleId,
        currencyText = truncate(item.currency, 64),
        visibleText = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeDigestViews(items: MutableList<EvChargeDigest>): MutableList<EvChargeDigestView> {
    val views = mutableListOf<EvChargeDigestView>()
    for (item in items) {
        views.add(toEvChargeDigestView(item))
    }
    return views
}

fun sumEvChargeDigestView(view: EvChargeDigestView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedDiscount + parsedOdometer.toDouble() + parsedWeight
}
