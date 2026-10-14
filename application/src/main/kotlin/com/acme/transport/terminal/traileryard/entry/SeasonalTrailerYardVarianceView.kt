package com.acme.transport.terminal.traileryard.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal trailer yard variance. */
data class SeasonalTrailerYardVarianceView(
    var code: String,
    var name: String,
    var costText: String,
    var driverIdText: String,
    var descriptionText: String,
    var weight: String,
    var etaMinutes: String,
    var amount: String,
    var marginText: String,
    var ownerId: String,
    var country: String,
    var statusLabel: String,
)

fun toSeasonalTrailerYardVarianceView(item: SeasonalTrailerYardVariance): SeasonalTrailerYardVarianceView {
    return SeasonalTrailerYardVarianceView(
        code = truncate(item.code, 80),
        name = item.name,
        costText = "%.2f".format(item.cost),
        driverIdText = truncate(item.driverId, 120),
        descriptionText = truncate(item.description, 80),
        weight = "%.2f".format(item.weight),
        etaMinutes = item.etaMinutes.toString(),
        amount = "%.2f".format(item.amount),
        marginText = "%.2f".format(item.margin),
        ownerId = item.ownerId,
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalTrailerYardVarianceViews(items: MutableList<SeasonalTrailerYardVariance>): MutableList<SeasonalTrailerYardVarianceView> {
    val views = mutableListOf<SeasonalTrailerYardVarianceView>()
    for (item in items) {
        views.add(toSeasonalTrailerYardVarianceView(item))
    }
    return views
}

fun sumSeasonalTrailerYardVarianceView(view: SeasonalTrailerYardVarianceView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedWeight = view.weight.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedCost + parsedWeight + parsedEtaMinutes.toDouble()
}
