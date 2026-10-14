package com.acme.transport.network.zone.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered zone reservation. */
data class TieredZoneReservationView(
    var code: String,
    var title: String,
    var ratio: String,
    var revision: String,
    var description: String,
    var fuelLitersText: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toTieredZoneReservationView(item: TieredZoneReservation): TieredZoneReservationView {
    return TieredZoneReservationView(
        code = item.code,
        title = item.title,
        ratio = "%.2f".format(item.ratio),
        revision = item.revision.toString(),
        description = truncate(item.description, 40),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredZoneReservationViews(items: MutableList<TieredZoneReservation>): MutableList<TieredZoneReservationView> {
    val views = mutableListOf<TieredZoneReservationView>()
    for (item in items) {
        views.add(toTieredZoneReservationView(item))
    }
    return views
}

fun sumTieredZoneReservationView(view: TieredZoneReservationView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedRatio + parsedRevision.toDouble() + parsedFuelLiters
}
