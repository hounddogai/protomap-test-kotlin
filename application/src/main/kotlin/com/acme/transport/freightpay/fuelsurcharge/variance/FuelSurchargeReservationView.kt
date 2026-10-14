package com.acme.transport.freightpay.fuelsurcharge.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge reservation. */
data class FuelSurchargeReservationView(
    var code: String,
    var name: String,
    var dock: String,
    var currency: String,
    var attempts: String,
    var statusLabel: String,
)

fun toFuelSurchargeReservationView(item: FuelSurchargeReservation): FuelSurchargeReservationView {
    return FuelSurchargeReservationView(
        code = item.code,
        name = item.name,
        dock = item.dock,
        currency = truncate(item.currency, 64),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeReservationViews(items: MutableList<FuelSurchargeReservation>): MutableList<FuelSurchargeReservationView> {
    val views = mutableListOf<FuelSurchargeReservationView>()
    for (item in items) {
        views.add(toFuelSurchargeReservationView(item))
    }
    return views
}

fun sumFuelSurchargeReservationView(view: FuelSurchargeReservationView): Double {
    val parsedAttempts = view.attempts.toInt()
    return parsedAttempts.toDouble()
}
