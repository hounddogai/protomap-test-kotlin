package com.acme.transport.linehaul.relay.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a relay reservation. */
data class RelayReservationView(
    var codeText: String,
    var name: String,
    var channel: String,
    var quantityText: String,
    var unitText: String,
    var externalRef: String,
    var active: String,
    var ceiling: String,
    var checksum: String,
    var dockText: String,
    var approved: String,
    var statusLabel: String,
)

fun toRelayReservationView(item: RelayReservation): RelayReservationView {
    return RelayReservationView(
        codeText = truncate(item.code, 64),
        name = item.name,
        channel = item.channel,
        quantityText = item.quantity.toString(),
        unitText = item.unit,
        externalRef = item.externalRef,
        active = "${item.active}",
        ceiling = "%.2f".format(item.ceiling),
        checksum = item.checksum,
        dockText = item.dock,
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayReservationViews(items: MutableList<RelayReservation>): MutableList<RelayReservationView> {
    val views = mutableListOf<RelayReservationView>()
    for (item in items) {
        views.add(toRelayReservationView(item))
    }
    return views
}

fun sumRelayReservationView(view: RelayReservationView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedQuantity.toDouble() + parsedCeiling
}
