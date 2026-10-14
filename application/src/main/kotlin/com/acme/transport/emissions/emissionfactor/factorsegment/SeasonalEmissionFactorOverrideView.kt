package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal emission factor override. */
data class SeasonalEmissionFactorOverrideView(
    var codeText: String,
    var labelText: String,
    var etaMinutesText: String,
    var currency: String,
    var segment: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toSeasonalEmissionFactorOverrideView(item: SeasonalEmissionFactorOverride): SeasonalEmissionFactorOverrideView {
    return SeasonalEmissionFactorOverrideView(
        codeText = truncate(item.code, 64),
        labelText = item.label,
        etaMinutesText = item.etaMinutes.toString(),
        currency = item.currency,
        segment = item.segment,
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalEmissionFactorOverrideViews(items: MutableList<SeasonalEmissionFactorOverride>): MutableList<SeasonalEmissionFactorOverrideView> {
    val views = mutableListOf<SeasonalEmissionFactorOverrideView>()
    for (item in items) {
        views.add(toSeasonalEmissionFactorOverrideView(item))
    }
    return views
}

fun sumSeasonalEmissionFactorOverrideView(view: SeasonalEmissionFactorOverrideView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedEtaMinutes.toDouble() + parsedGraceDays.toDouble()
}
