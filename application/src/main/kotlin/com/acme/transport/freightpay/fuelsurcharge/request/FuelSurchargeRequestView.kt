package com.acme.transport.freightpay.fuelsurcharge.request

/** How screens and exports show a fuel surcharge request. */
data class FuelSurchargeRequestView(
    var code: String,
    var label: String,
    var fuelLiters: String,
    var endsAtText: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toFuelSurchargeRequestView(item: FuelSurchargeRequest): FuelSurchargeRequestView {
    return FuelSurchargeRequestView(
        code = item.code,
        label = item.label,
        fuelLiters = "%.2f".format(item.fuelLiters),
        endsAtText = item.endsAt.toString(),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeRequestViews(items: MutableList<FuelSurchargeRequest>): MutableList<FuelSurchargeRequestView> {
    val views = mutableListOf<FuelSurchargeRequestView>()
    for (item in items) {
        views.add(toFuelSurchargeRequestView(item))
    }
    return views
}

fun sumFuelSurchargeRequestView(view: FuelSurchargeRequestView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedFuelLiters + parsedEndsAt.toDouble() + parsedEtaMinutes.toDouble()
}
