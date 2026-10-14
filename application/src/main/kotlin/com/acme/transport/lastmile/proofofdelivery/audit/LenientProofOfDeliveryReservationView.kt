package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient proof of delivery reservation. */
data class LenientProofOfDeliveryReservationView(
    var code: String,
    var title: String,
    var startsAt: String,
    var regionText: String,
    var vehicleId: String,
    var approved: String,
    var labelText: String,
    var country: String,
    var odometerText: String,
    var cost: String,
    var statusLabel: String,
)

fun toLenientProofOfDeliveryReservationView(item: LenientProofOfDeliveryReservation): LenientProofOfDeliveryReservationView {
    return LenientProofOfDeliveryReservationView(
        code = item.code,
        title = truncate(item.title, 120),
        startsAt = item.startsAt.toString(),
        regionText = truncate(item.region, 80),
        vehicleId = item.vehicleId,
        approved = "${item.approved}",
        labelText = item.label,
        country = item.country,
        odometerText = item.odometer.toString(),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientProofOfDeliveryReservationViews(items: MutableList<LenientProofOfDeliveryReservation>): MutableList<LenientProofOfDeliveryReservationView> {
    val views = mutableListOf<LenientProofOfDeliveryReservationView>()
    for (item in items) {
        views.add(toLenientProofOfDeliveryReservationView(item))
    }
    return views
}

fun sumLenientProofOfDeliveryReservationView(view: LenientProofOfDeliveryReservationView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedOdometer = view.odometerText.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedStartsAt.toDouble() + parsedOdometer.toDouble() + parsedCost
}
