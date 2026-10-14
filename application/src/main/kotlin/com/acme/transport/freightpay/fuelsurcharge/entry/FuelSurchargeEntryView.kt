package com.acme.transport.freightpay.fuelsurcharge.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge entry. */
data class FuelSurchargeEntryView(
    var code: String,
    var title: String,
    var distanceKmText: String,
    var retriesText: String,
    var currencyText: String,
    var pinnedText: String,
    var floorValueText: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toFuelSurchargeEntryView(item: FuelSurchargeEntry): FuelSurchargeEntryView {
    return FuelSurchargeEntryView(
        code = truncate(item.code, 120),
        title = item.title,
        distanceKmText = "%.2f".format(item.distanceKm),
        retriesText = item.retries.toString(),
        currencyText = item.currency,
        pinnedText = "${item.pinned}",
        floorValueText = "%.2f".format(item.floorValue),
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeEntryViews(items: MutableList<FuelSurchargeEntry>): MutableList<FuelSurchargeEntryView> {
    val views = mutableListOf<FuelSurchargeEntryView>()
    for (item in items) {
        views.add(toFuelSurchargeEntryView(item))
    }
    return views
}

fun sumFuelSurchargeEntryView(view: FuelSurchargeEntryView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedRetries = view.retriesText.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedDistanceKm + parsedRetries.toDouble() + parsedFloorValue
}
