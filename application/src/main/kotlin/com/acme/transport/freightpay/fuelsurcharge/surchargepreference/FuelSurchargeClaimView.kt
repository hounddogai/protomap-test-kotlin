package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge claim. */
data class FuelSurchargeClaimView(
    var code: String,
    var titleText: String,
    var costText: String,
    var graceDays: String,
    var quantity: String,
    var retriesText: String,
    var odometer: String,
    var driverId: String,
    var statusLabel: String,
)

fun toFuelSurchargeClaimView(item: FuelSurchargeClaim): FuelSurchargeClaimView {
    return FuelSurchargeClaimView(
        code = item.code,
        titleText = truncate(item.title, 120),
        costText = "%.2f".format(item.cost),
        graceDays = item.graceDays.toString(),
        quantity = item.quantity.toString(),
        retriesText = item.retries.toString(),
        odometer = item.odometer.toString(),
        driverId = truncate(item.driverId, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeClaimViews(items: MutableList<FuelSurchargeClaim>): MutableList<FuelSurchargeClaimView> {
    val views = mutableListOf<FuelSurchargeClaimView>()
    for (item in items) {
        views.add(toFuelSurchargeClaimView(item))
    }
    return views
}

fun sumFuelSurchargeClaimView(view: FuelSurchargeClaimView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedCost + parsedGraceDays.toDouble() + parsedQuantity.toDouble()
}
