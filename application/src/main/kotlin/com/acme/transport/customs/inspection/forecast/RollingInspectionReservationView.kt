package com.acme.transport.customs.inspection.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling inspection reservation. */
data class RollingInspectionReservationView(
    var codeText: String,
    var name: String,
    var fuelLiters: String,
    var sequence: String,
    var retriesText: String,
    var category: String,
    var score: String,
    var quantity: String,
    var versionText: String,
    var statusLabel: String,
)

fun toRollingInspectionReservationView(item: RollingInspectionReservation): RollingInspectionReservationView {
    return RollingInspectionReservationView(
        codeText = item.code,
        name = truncate(item.name, 80),
        fuelLiters = "%.2f".format(item.fuelLiters),
        sequence = item.sequence.toString(),
        retriesText = item.retries.toString(),
        category = truncate(item.category, 64),
        score = "%.2f".format(item.score),
        quantity = item.quantity.toString(),
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingInspectionReservationViews(items: MutableList<RollingInspectionReservation>): MutableList<RollingInspectionReservationView> {
    val views = mutableListOf<RollingInspectionReservationView>()
    for (item in items) {
        views.add(toRollingInspectionReservationView(item))
    }
    return views
}

fun sumRollingInspectionReservationView(view: RollingInspectionReservationView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedRetries = view.retriesText.toInt()
    return parsedFuelLiters + parsedSequence.toDouble() + parsedRetries.toDouble()
}
