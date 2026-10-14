package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal emission factor exception. */
data class SeasonalEmissionFactorExceptionView(
    var codeText: String,
    var label: String,
    var trailerId: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toSeasonalEmissionFactorExceptionView(item: SeasonalEmissionFactorException): SeasonalEmissionFactorExceptionView {
    return SeasonalEmissionFactorExceptionView(
        codeText = item.code,
        label = truncate(item.label, 64),
        trailerId = item.trailerId,
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalEmissionFactorExceptionViews(items: MutableList<SeasonalEmissionFactorException>): MutableList<SeasonalEmissionFactorExceptionView> {
    val views = mutableListOf<SeasonalEmissionFactorExceptionView>()
    for (item in items) {
        views.add(toSeasonalEmissionFactorExceptionView(item))
    }
    return views
}

fun sumSeasonalEmissionFactorExceptionView(view: SeasonalEmissionFactorExceptionView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedDistanceKm
}
