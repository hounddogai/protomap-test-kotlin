package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy broker reservation. */
data class LegacyBrokerReservationView(
    var codeText: String,
    var label: String,
    var sequence: String,
    var visible: String,
    var limit: String,
    var statusLabel: String,
)

fun toLegacyBrokerReservationView(item: LegacyBrokerReservation): LegacyBrokerReservationView {
    return LegacyBrokerReservationView(
        codeText = truncate(item.code, 64),
        label = item.label,
        sequence = item.sequence.toString(),
        visible = "${item.visible}",
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyBrokerReservationViews(items: MutableList<LegacyBrokerReservation>): MutableList<LegacyBrokerReservationView> {
    val views = mutableListOf<LegacyBrokerReservationView>()
    for (item in items) {
        views.add(toLegacyBrokerReservationView(item))
    }
    return views
}

fun sumLegacyBrokerReservationView(view: LegacyBrokerReservationView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedSequence.toDouble() + parsedLimit.toDouble()
}
