package com.acme.transport.freightpay.fuelsurcharge.review

/** How screens and exports show a legacy fuel surcharge window. */
data class LegacyFuelSurchargeWindowView(
    var code: String,
    var nameText: String,
    var lane: String,
    var retriesText: String,
    var startsAtText: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toLegacyFuelSurchargeWindowView(item: LegacyFuelSurchargeWindow): LegacyFuelSurchargeWindowView {
    return LegacyFuelSurchargeWindowView(
        code = item.code,
        nameText = item.name,
        lane = item.lane,
        retriesText = item.retries.toString(),
        startsAtText = item.startsAt.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyFuelSurchargeWindowViews(items: MutableList<LegacyFuelSurchargeWindow>): MutableList<LegacyFuelSurchargeWindowView> {
    val views = mutableListOf<LegacyFuelSurchargeWindowView>()
    for (item in items) {
        views.add(toLegacyFuelSurchargeWindowView(item))
    }
    return views
}

fun sumLegacyFuelSurchargeWindowView(view: LegacyFuelSurchargeWindowView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedRetries.toDouble() + parsedStartsAt.toDouble() + parsedFuelLiters
}
