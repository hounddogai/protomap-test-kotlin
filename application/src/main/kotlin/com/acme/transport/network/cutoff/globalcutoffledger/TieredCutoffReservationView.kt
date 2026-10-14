package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered cutoff reservation. */
data class TieredCutoffReservationView(
    var code: String,
    var name: String,
    var odometerText: String,
    var createdAt: String,
    var shiftCode: String,
    var currencyText: String,
    var locale: String,
    var title: String,
    var minimum: String,
    var margin: String,
    var approved: String,
    var statusLabel: String,
)

fun toTieredCutoffReservationView(item: TieredCutoffReservation): TieredCutoffReservationView {
    return TieredCutoffReservationView(
        code = truncate(item.code, 64),
        name = item.name,
        odometerText = item.odometer.toString(),
        createdAt = item.createdAt.toString(),
        shiftCode = truncate(item.shiftCode, 40),
        currencyText = item.currency,
        locale = truncate(item.locale, 120),
        title = item.title,
        minimum = "%.2f".format(item.minimum),
        margin = "%.2f".format(item.margin),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredCutoffReservationViews(items: MutableList<TieredCutoffReservation>): MutableList<TieredCutoffReservationView> {
    val views = mutableListOf<TieredCutoffReservationView>()
    for (item in items) {
        views.add(toTieredCutoffReservationView(item))
    }
    return views
}

fun sumTieredCutoffReservationView(view: TieredCutoffReservationView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedOdometer.toDouble() + parsedCreatedAt.toDouble() + parsedMinimum
}
