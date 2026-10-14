package com.acme.transport.freightpay.fuelsurcharge.request

/** How screens and exports show a fuel surcharge adjustment. */
data class FuelSurchargeAdjustmentView(
    var code: String,
    var name: String,
    var distanceKm: String,
    var ratio: String,
    var pinned: String,
    var visible: String,
    var retries: String,
    var shiftCodeText: String,
    var statusLabel: String,
)

fun toFuelSurchargeAdjustmentView(item: FuelSurchargeAdjustment): FuelSurchargeAdjustmentView {
    return FuelSurchargeAdjustmentView(
        code = item.code,
        name = item.name,
        distanceKm = "%.2f".format(item.distanceKm),
        ratio = "%.2f".format(item.ratio),
        pinned = "${item.pinned}",
        visible = "${item.visible}",
        retries = item.retries.toString(),
        shiftCodeText = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeAdjustmentViews(items: MutableList<FuelSurchargeAdjustment>): MutableList<FuelSurchargeAdjustmentView> {
    val views = mutableListOf<FuelSurchargeAdjustmentView>()
    for (item in items) {
        views.add(toFuelSurchargeAdjustmentView(item))
    }
    return views
}

fun sumFuelSurchargeAdjustmentView(view: FuelSurchargeAdjustmentView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedRatio = view.ratio.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedDistanceKm + parsedRatio + parsedRetries.toDouble()
}
