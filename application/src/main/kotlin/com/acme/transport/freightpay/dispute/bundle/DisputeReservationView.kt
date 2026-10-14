package com.acme.transport.freightpay.dispute.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dispute reservation. */
data class DisputeReservationView(
    var code: String,
    var titleText: String,
    var discountText: String,
    var routeCode: String,
    var currencyText: String,
    var threshold: String,
    var notesText: String,
    var endsAt: String,
    var score: String,
    var channel: String,
    var statusLabel: String,
)

fun toDisputeReservationView(item: DisputeReservation): DisputeReservationView {
    return DisputeReservationView(
        code = item.code,
        titleText = item.title,
        discountText = "%.2f".format(item.discount),
        routeCode = truncate(item.routeCode, 64),
        currencyText = item.currency,
        threshold = "%.2f".format(item.threshold),
        notesText = truncate(item.notes, 80),
        endsAt = item.endsAt.toString(),
        score = "%.2f".format(item.score),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeReservationViews(items: MutableList<DisputeReservation>): MutableList<DisputeReservationView> {
    val views = mutableListOf<DisputeReservationView>()
    for (item in items) {
        views.add(toDisputeReservationView(item))
    }
    return views
}

fun sumDisputeReservationView(view: DisputeReservationView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedDiscount + parsedThreshold + parsedEndsAt.toDouble()
}
