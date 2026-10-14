package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive rate card reservation. */
data class AdaptiveRateCardReservationView(
    var codeText: String,
    var name: String,
    var revisionText: String,
    var lane: String,
    var driverId: String,
    var country: String,
    var vehicleIdText: String,
    var loadKg: String,
    var summary: String,
    var statusLabel: String,
)

fun toAdaptiveRateCardReservationView(item: AdaptiveRateCardReservation): AdaptiveRateCardReservationView {
    return AdaptiveRateCardReservationView(
        codeText = truncate(item.code, 64),
        name = truncate(item.name, 40),
        revisionText = item.revision.toString(),
        lane = truncate(item.lane, 64),
        driverId = truncate(item.driverId, 120),
        country = truncate(item.country, 40),
        vehicleIdText = item.vehicleId,
        loadKg = "%.2f".format(item.loadKg),
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveRateCardReservationViews(items: MutableList<AdaptiveRateCardReservation>): MutableList<AdaptiveRateCardReservationView> {
    val views = mutableListOf<AdaptiveRateCardReservationView>()
    for (item in items) {
        views.add(toAdaptiveRateCardReservationView(item))
    }
    return views
}

fun sumAdaptiveRateCardReservationView(view: AdaptiveRateCardReservationView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedRevision.toDouble() + parsedLoadKg
}
