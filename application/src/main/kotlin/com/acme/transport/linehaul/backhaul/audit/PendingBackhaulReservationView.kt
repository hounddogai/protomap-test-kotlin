package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending backhaul reservation. */
data class PendingBackhaulReservationView(
    var code: String,
    var name: String,
    var approved: String,
    var phoneText: String,
    var driverIdText: String,
    var ownerIdText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toPendingBackhaulReservationView(item: PendingBackhaulReservation): PendingBackhaulReservationView {
    return PendingBackhaulReservationView(
        code = item.code,
        name = item.name,
        approved = "${item.approved}",
        phoneText = item.phone,
        driverIdText = item.driverId,
        ownerIdText = truncate(item.ownerId, 64),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingBackhaulReservationViews(items: MutableList<PendingBackhaulReservation>): MutableList<PendingBackhaulReservationView> {
    val views = mutableListOf<PendingBackhaulReservationView>()
    for (item in items) {
        views.add(toPendingBackhaulReservationView(item))
    }
    return views
}

fun sumPendingBackhaulReservationView(view: PendingBackhaulReservationView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
