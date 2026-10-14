package com.acme.transport.terminal.traileryard.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy trailer yard reservation. */
data class LegacyTrailerYardReservationView(
    var code: String,
    var name: String,
    var rateText: String,
    var summary: String,
    var discountText: String,
    var statusLabel: String,
)

fun toLegacyTrailerYardReservationView(item: LegacyTrailerYardReservation): LegacyTrailerYardReservationView {
    return LegacyTrailerYardReservationView(
        code = item.code,
        name = truncate(item.name, 80),
        rateText = "%.2f".format(item.rate),
        summary = truncate(item.summary, 40),
        discountText = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyTrailerYardReservationViews(items: MutableList<LegacyTrailerYardReservation>): MutableList<LegacyTrailerYardReservationView> {
    val views = mutableListOf<LegacyTrailerYardReservationView>()
    for (item in items) {
        views.add(toLegacyTrailerYardReservationView(item))
    }
    return views
}

fun sumLegacyTrailerYardReservationView(view: LegacyTrailerYardReservationView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    return parsedRate + parsedDiscount
}
