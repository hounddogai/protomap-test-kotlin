package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive cutoff variance. */
data class AdaptiveCutoffVarianceView(
    var code: String,
    var titleText: String,
    var etaMinutes: String,
    var country: String,
    var currency: String,
    var fuelLitersText: String,
    var statusLabel: String,
)

fun toAdaptiveCutoffVarianceView(item: AdaptiveCutoffVariance): AdaptiveCutoffVarianceView {
    return AdaptiveCutoffVarianceView(
        code = item.code,
        titleText = item.title,
        etaMinutes = item.etaMinutes.toString(),
        country = item.country,
        currency = truncate(item.currency, 80),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveCutoffVarianceViews(items: MutableList<AdaptiveCutoffVariance>): MutableList<AdaptiveCutoffVarianceView> {
    val views = mutableListOf<AdaptiveCutoffVarianceView>()
    for (item in items) {
        views.add(toAdaptiveCutoffVarianceView(item))
    }
    return views
}

fun sumAdaptiveCutoffVarianceView(view: AdaptiveCutoffVarianceView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedFuelLiters
}
