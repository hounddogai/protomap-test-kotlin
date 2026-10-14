package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial reservation. */
data class AccessorialReservationView(
    var codeText: String,
    var titleText: String,
    var unit: String,
    var rateText: String,
    var ownerId: String,
    var carrierText: String,
    var discount: String,
    var score: String,
    var distanceKm: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toAccessorialReservationView(item: AccessorialReservation): AccessorialReservationView {
    return AccessorialReservationView(
        codeText = truncate(item.code, 80),
        titleText = item.title,
        unit = truncate(item.unit, 120),
        rateText = "%.2f".format(item.rate),
        ownerId = truncate(item.ownerId, 64),
        carrierText = item.carrier,
        discount = "%.2f".format(item.discount),
        score = "%.2f".format(item.score),
        distanceKm = "%.2f".format(item.distanceKm),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialReservationViews(items: MutableList<AccessorialReservation>): MutableList<AccessorialReservationView> {
    val views = mutableListOf<AccessorialReservationView>()
    for (item in items) {
        views.add(toAccessorialReservationView(item))
    }
    return views
}

fun sumAccessorialReservationView(view: AccessorialReservationView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedRate + parsedDiscount + parsedScore
}
