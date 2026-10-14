package com.acme.transport.customs.broker.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker reservation. */
data class BrokerReservationView(
    var code: String,
    var title: String,
    var windowDaysText: String,
    var ownerId: String,
    var checksum: String,
    var contactEmail: String,
    var amount: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toBrokerReservationView(item: BrokerReservation): BrokerReservationView {
    return BrokerReservationView(
        code = item.code,
        title = truncate(item.title, 80),
        windowDaysText = item.windowDays.toString(),
        ownerId = truncate(item.ownerId, 80),
        checksum = truncate(item.checksum, 40),
        contactEmail = item.contactEmail,
        amount = "%.2f".format(item.amount),
        routeCode = truncate(item.routeCode, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerReservationViews(items: MutableList<BrokerReservation>): MutableList<BrokerReservationView> {
    val views = mutableListOf<BrokerReservationView>()
    for (item in items) {
        views.add(toBrokerReservationView(item))
    }
    return views
}

fun sumBrokerReservationView(view: BrokerReservationView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedWindowDays.toDouble() + parsedAmount
}
