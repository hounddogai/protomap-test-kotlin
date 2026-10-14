package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy rate card target. */
data class LegacyRateCardTargetView(
    var code: String,
    var labelText: String,
    var ratio: String,
    var minimum: String,
    var etaMinutes: String,
    var fuelLitersText: String,
    var statusLabel: String,
)

fun toLegacyRateCardTargetView(item: LegacyRateCardTarget): LegacyRateCardTargetView {
    return LegacyRateCardTargetView(
        code = item.code,
        labelText = truncate(item.label, 64),
        ratio = "%.2f".format(item.ratio),
        minimum = "%.2f".format(item.minimum),
        etaMinutes = item.etaMinutes.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyRateCardTargetViews(items: MutableList<LegacyRateCardTarget>): MutableList<LegacyRateCardTargetView> {
    val views = mutableListOf<LegacyRateCardTargetView>()
    for (item in items) {
        views.add(toLegacyRateCardTargetView(item))
    }
    return views
}

fun sumLegacyRateCardTargetView(view: LegacyRateCardTargetView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedRatio + parsedMinimum + parsedEtaMinutes.toDouble()
}
