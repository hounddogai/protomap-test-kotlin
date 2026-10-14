package com.acme.transport.fleetops.tire.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling tire reservation. */
data class RollingTireReservationView(
    var codeText: String,
    var titleText: String,
    var attemptsText: String,
    var unitText: String,
    var pallets: String,
    var fuelLiters: String,
    var vehicleId: String,
    var summary: String,
    var statusLabel: String,
)

fun toRollingTireReservationView(item: RollingTireReservation): RollingTireReservationView {
    return RollingTireReservationView(
        codeText = truncate(item.code, 64),
        titleText = item.title,
        attemptsText = item.attempts.toString(),
        unitText = item.unit,
        pallets = item.pallets.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        vehicleId = item.vehicleId,
        summary = truncate(item.summary, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingTireReservationViews(items: MutableList<RollingTireReservation>): MutableList<RollingTireReservationView> {
    val views = mutableListOf<RollingTireReservationView>()
    for (item in items) {
        views.add(toRollingTireReservationView(item))
    }
    return views
}

fun sumRollingTireReservationView(view: RollingTireReservationView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedAttempts.toDouble() + parsedPallets.toDouble() + parsedFuelLiters
}
