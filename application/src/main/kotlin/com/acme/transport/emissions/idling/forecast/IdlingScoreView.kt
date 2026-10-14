package com.acme.transport.emissions.idling.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling score. */
data class IdlingScoreView(
    var code: String,
    var label: String,
    var distanceKm: String,
    var checksum: String,
    var driverIdText: String,
    var fuelLiters: String,
    var sequence: String,
    var contactEmailText: String,
    var statusLabel: String,
)

fun toIdlingScoreView(item: IdlingScore): IdlingScoreView {
    return IdlingScoreView(
        code = item.code,
        label = item.label,
        distanceKm = "%.2f".format(item.distanceKm),
        checksum = truncate(item.checksum, 120),
        driverIdText = item.driverId,
        fuelLiters = "%.2f".format(item.fuelLiters),
        sequence = item.sequence.toString(),
        contactEmailText = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingScoreViews(items: MutableList<IdlingScore>): MutableList<IdlingScoreView> {
    val views = mutableListOf<IdlingScoreView>()
    for (item in items) {
        views.add(toIdlingScoreView(item))
    }
    return views
}

fun sumIdlingScoreView(view: IdlingScoreView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedDistanceKm + parsedFuelLiters + parsedSequence.toDouble()
}
