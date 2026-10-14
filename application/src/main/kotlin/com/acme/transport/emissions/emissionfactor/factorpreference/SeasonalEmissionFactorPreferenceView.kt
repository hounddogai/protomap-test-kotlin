package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal emission factor preference. */
data class SeasonalEmissionFactorPreferenceView(
    var code: String,
    var titleText: String,
    var vehicleIdText: String,
    var stopCountText: String,
    var threshold: String,
    var amount: String,
    var statusLabel: String,
)

fun toSeasonalEmissionFactorPreferenceView(item: SeasonalEmissionFactorPreference): SeasonalEmissionFactorPreferenceView {
    return SeasonalEmissionFactorPreferenceView(
        code = truncate(item.code, 64),
        titleText = truncate(item.title, 80),
        vehicleIdText = item.vehicleId,
        stopCountText = item.stopCount.toString(),
        threshold = "%.2f".format(item.threshold),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalEmissionFactorPreferenceViews(items: MutableList<SeasonalEmissionFactorPreference>): MutableList<SeasonalEmissionFactorPreferenceView> {
    val views = mutableListOf<SeasonalEmissionFactorPreferenceView>()
    for (item in items) {
        views.add(toSeasonalEmissionFactorPreferenceView(item))
    }
    return views
}

fun sumSeasonalEmissionFactorPreferenceView(view: SeasonalEmissionFactorPreferenceView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedStopCount.toDouble() + parsedThreshold + parsedAmount
}
