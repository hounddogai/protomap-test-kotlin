package com.acme.transport.network.node.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal node agreement. */
data class SeasonalNodeAgreementView(
    var codeText: String,
    var labelText: String,
    var fuelLiters: String,
    var driverId: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toSeasonalNodeAgreementView(item: SeasonalNodeAgreement): SeasonalNodeAgreementView {
    return SeasonalNodeAgreementView(
        codeText = truncate(item.code, 64),
        labelText = truncate(item.label, 80),
        fuelLiters = "%.2f".format(item.fuelLiters),
        driverId = item.driverId,
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalNodeAgreementViews(items: MutableList<SeasonalNodeAgreement>): MutableList<SeasonalNodeAgreementView> {
    val views = mutableListOf<SeasonalNodeAgreementView>()
    for (item in items) {
        views.add(toSeasonalNodeAgreementView(item))
    }
    return views
}

fun sumSeasonalNodeAgreementView(view: SeasonalNodeAgreementView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedFuelLiters + parsedDistanceKm
}
