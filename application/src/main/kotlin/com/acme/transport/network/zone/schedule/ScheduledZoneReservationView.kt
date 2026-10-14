package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled zone reservation. */
data class ScheduledZoneReservationView(
    var codeText: String,
    var labelText: String,
    var pinnedText: String,
    var carrier: String,
    var loadKgText: String,
    var retries: String,
    var amount: String,
    var maximum: String,
    var odometerText: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toScheduledZoneReservationView(item: ScheduledZoneReservation): ScheduledZoneReservationView {
    return ScheduledZoneReservationView(
        codeText = item.code,
        labelText = item.label,
        pinnedText = "${item.pinned}",
        carrier = truncate(item.carrier, 40),
        loadKgText = "%.2f".format(item.loadKg),
        retries = item.retries.toString(),
        amount = "%.2f".format(item.amount),
        maximum = "%.2f".format(item.maximum),
        odometerText = item.odometer.toString(),
        currencyText = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledZoneReservationViews(items: MutableList<ScheduledZoneReservation>): MutableList<ScheduledZoneReservationView> {
    val views = mutableListOf<ScheduledZoneReservationView>()
    for (item in items) {
        views.add(toScheduledZoneReservationView(item))
    }
    return views
}

fun sumScheduledZoneReservationView(view: ScheduledZoneReservationView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedLoadKg + parsedRetries.toDouble() + parsedAmount
}
