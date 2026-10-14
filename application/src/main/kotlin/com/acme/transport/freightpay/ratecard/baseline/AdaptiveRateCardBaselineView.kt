package com.acme.transport.freightpay.ratecard.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive rate card baseline. */
data class AdaptiveRateCardBaselineView(
    var code: String,
    var nameText: String,
    var fuelLiters: String,
    var channel: String,
    var ceiling: String,
    var vehicleId: String,
    var labelText: String,
    var statusLabel: String,
)

fun toAdaptiveRateCardBaselineView(item: AdaptiveRateCardBaseline): AdaptiveRateCardBaselineView {
    return AdaptiveRateCardBaselineView(
        code = truncate(item.code, 40),
        nameText = item.name,
        fuelLiters = "%.2f".format(item.fuelLiters),
        channel = item.channel,
        ceiling = "%.2f".format(item.ceiling),
        vehicleId = truncate(item.vehicleId, 80),
        labelText = truncate(item.label, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveRateCardBaselineViews(items: MutableList<AdaptiveRateCardBaseline>): MutableList<AdaptiveRateCardBaselineView> {
    val views = mutableListOf<AdaptiveRateCardBaselineView>()
    for (item in items) {
        views.add(toAdaptiveRateCardBaselineView(item))
    }
    return views
}

fun sumAdaptiveRateCardBaselineView(view: AdaptiveRateCardBaselineView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedFuelLiters + parsedCeiling
}
