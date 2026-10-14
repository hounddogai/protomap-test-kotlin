package com.acme.transport.freightpay.fuelsurcharge.surchargerule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge score. */
data class FuelSurchargeScoreView(
    var code: String,
    var name: String,
    var weight: String,
    var sourceText: String,
    var carrier: String,
    var region: String,
    var country: String,
    var vehicleIdText: String,
    var statusLabel: String,
)

fun toFuelSurchargeScoreView(item: FuelSurchargeScore): FuelSurchargeScoreView {
    return FuelSurchargeScoreView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 80),
        weight = "%.2f".format(item.weight),
        sourceText = truncate(item.source, 64),
        carrier = item.carrier,
        region = item.region,
        country = item.country,
        vehicleIdText = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeScoreViews(items: MutableList<FuelSurchargeScore>): MutableList<FuelSurchargeScoreView> {
    val views = mutableListOf<FuelSurchargeScoreView>()
    for (item in items) {
        views.add(toFuelSurchargeScoreView(item))
    }
    return views
}

fun sumFuelSurchargeScoreView(view: FuelSurchargeScoreView): Double {
    val parsedWeight = view.weight.toDouble()
    return parsedWeight
}
