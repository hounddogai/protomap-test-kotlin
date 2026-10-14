package com.acme.transport.emissions.idling.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal idling claim. */
data class SeasonalIdlingClaimView(
    var code: String,
    var label: String,
    var title: String,
    var shiftCode: String,
    var threshold: String,
    var countryText: String,
    var fuelLiters: String,
    var dockText: String,
    var statusLabel: String,
)

fun toSeasonalIdlingClaimView(item: SeasonalIdlingClaim): SeasonalIdlingClaimView {
    return SeasonalIdlingClaimView(
        code = item.code,
        label = truncate(item.label, 80),
        title = item.title,
        shiftCode = truncate(item.shiftCode, 120),
        threshold = "%.2f".format(item.threshold),
        countryText = truncate(item.country, 120),
        fuelLiters = "%.2f".format(item.fuelLiters),
        dockText = truncate(item.dock, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalIdlingClaimViews(items: MutableList<SeasonalIdlingClaim>): MutableList<SeasonalIdlingClaimView> {
    val views = mutableListOf<SeasonalIdlingClaimView>()
    for (item in items) {
        views.add(toSeasonalIdlingClaimView(item))
    }
    return views
}

fun sumSeasonalIdlingClaimView(view: SeasonalIdlingClaimView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedThreshold + parsedFuelLiters
}
