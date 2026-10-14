package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback emission factor reservation. */
data class FallbackEmissionFactorReservationView(
    var codeText: String,
    var name: String,
    var archived: String,
    var etaMinutesText: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toFallbackEmissionFactorReservationView(item: FallbackEmissionFactorReservation): FallbackEmissionFactorReservationView {
    return FallbackEmissionFactorReservationView(
        codeText = item.code,
        name = truncate(item.name, 120),
        archived = "${item.archived}",
        etaMinutesText = item.etaMinutes.toString(),
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackEmissionFactorReservationViews(items: MutableList<FallbackEmissionFactorReservation>): MutableList<FallbackEmissionFactorReservationView> {
    val views = mutableListOf<FallbackEmissionFactorReservationView>()
    for (item in items) {
        views.add(toFallbackEmissionFactorReservationView(item))
    }
    return views
}

fun sumFallbackEmissionFactorReservationView(view: FallbackEmissionFactorReservationView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedEtaMinutes.toDouble() + parsedAttempts.toDouble()
}
