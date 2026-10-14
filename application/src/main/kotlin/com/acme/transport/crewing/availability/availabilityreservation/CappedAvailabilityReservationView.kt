package com.acme.transport.crewing.availability.availabilityreservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped availability reservation. */
data class CappedAvailabilityReservationView(
    var codeText: String,
    var nameText: String,
    var shiftCode: String,
    var contactEmailText: String,
    var locale: String,
    var batchSize: String,
    var notes: String,
    var distanceKmText: String,
    var minimum: String,
    var startsAt: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toCappedAvailabilityReservationView(item: CappedAvailabilityReservation): CappedAvailabilityReservationView {
    return CappedAvailabilityReservationView(
        codeText = item.code,
        nameText = item.name,
        shiftCode = item.shiftCode,
        contactEmailText = truncate(item.contactEmail, 80),
        locale = item.locale,
        batchSize = item.batchSize.toString(),
        notes = truncate(item.notes, 120),
        distanceKmText = "%.2f".format(item.distanceKm),
        minimum = "%.2f".format(item.minimum),
        startsAt = item.startsAt.toString(),
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedAvailabilityReservationViews(items: MutableList<CappedAvailabilityReservation>): MutableList<CappedAvailabilityReservationView> {
    val views = mutableListOf<CappedAvailabilityReservationView>()
    for (item in items) {
        views.add(toCappedAvailabilityReservationView(item))
    }
    return views
}

fun sumCappedAvailabilityReservationView(view: CappedAvailabilityReservationView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedBatchSize.toDouble() + parsedDistanceKm + parsedMinimum
}
