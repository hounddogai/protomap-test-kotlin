package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor assignment. */
data class EmissionFactorAssignmentView(
    var code: String,
    var name: String,
    var expiresAt: String,
    var distanceKm: String,
    var score: String,
    var etaMinutes: String,
    var ceiling: String,
    var titleText: String,
    var driverIdText: String,
    var currencyText: String,
    var odometer: String,
    var statusLabel: String,
)

fun toEmissionFactorAssignmentView(item: EmissionFactorAssignment): EmissionFactorAssignmentView {
    return EmissionFactorAssignmentView(
        code = item.code,
        name = truncate(item.name, 120),
        expiresAt = item.expiresAt.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        score = "%.2f".format(item.score),
        etaMinutes = item.etaMinutes.toString(),
        ceiling = "%.2f".format(item.ceiling),
        titleText = item.title,
        driverIdText = truncate(item.driverId, 40),
        currencyText = truncate(item.currency, 80),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorAssignmentViews(items: MutableList<EmissionFactorAssignment>): MutableList<EmissionFactorAssignmentView> {
    val views = mutableListOf<EmissionFactorAssignmentView>()
    for (item in items) {
        views.add(toEmissionFactorAssignmentView(item))
    }
    return views
}

fun sumEmissionFactorAssignmentView(view: EmissionFactorAssignmentView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedExpiresAt.toDouble() + parsedDistanceKm + parsedScore
}
