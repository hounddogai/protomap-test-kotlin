package com.acme.transport.network.servicearea.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient service area request. */
data class LenientServiceAreaRequestView(
    var code: String,
    var labelText: String,
    var timezone: String,
    var pallets: String,
    var updatedAt: String,
    var unit: String,
    var fuelLiters: String,
    var minimum: String,
    var quantity: String,
    var statusLabel: String,
)

fun toLenientServiceAreaRequestView(item: LenientServiceAreaRequest): LenientServiceAreaRequestView {
    return LenientServiceAreaRequestView(
        code = truncate(item.code, 120),
        labelText = item.label,
        timezone = truncate(item.timezone, 64),
        pallets = item.pallets.toString(),
        updatedAt = item.updatedAt.toString(),
        unit = item.unit,
        fuelLiters = "%.2f".format(item.fuelLiters),
        minimum = "%.2f".format(item.minimum),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientServiceAreaRequestViews(items: MutableList<LenientServiceAreaRequest>): MutableList<LenientServiceAreaRequestView> {
    val views = mutableListOf<LenientServiceAreaRequestView>()
    for (item in items) {
        views.add(toLenientServiceAreaRequestView(item))
    }
    return views
}

fun sumLenientServiceAreaRequestView(view: LenientServiceAreaRequestView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedPallets.toDouble() + parsedUpdatedAt.toDouble() + parsedFuelLiters
}
