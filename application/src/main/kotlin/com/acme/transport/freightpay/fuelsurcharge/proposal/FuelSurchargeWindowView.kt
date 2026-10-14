package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge window. */
data class FuelSurchargeWindowView(
    var code: String,
    var name: String,
    var dock: String,
    var ownerIdText: String,
    var title: String,
    var country: String,
    var description: String,
    var thresholdText: String,
    var startsAt: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toFuelSurchargeWindowView(item: FuelSurchargeWindow): FuelSurchargeWindowView {
    return FuelSurchargeWindowView(
        code = truncate(item.code, 64),
        name = truncate(item.name, 80),
        dock = truncate(item.dock, 40),
        ownerIdText = truncate(item.ownerId, 40),
        title = item.title,
        country = item.country,
        description = item.description,
        thresholdText = "%.2f".format(item.threshold),
        startsAt = item.startsAt.toString(),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeWindowViews(items: MutableList<FuelSurchargeWindow>): MutableList<FuelSurchargeWindowView> {
    val views = mutableListOf<FuelSurchargeWindowView>()
    for (item in items) {
        views.add(toFuelSurchargeWindowView(item))
    }
    return views
}

fun sumFuelSurchargeWindowView(view: FuelSurchargeWindowView): Double {
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedThreshold + parsedStartsAt.toDouble()
}
