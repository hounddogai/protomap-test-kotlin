package com.acme.transport.freightpay.dispute.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global dispute reservation. */
data class GlobalDisputeReservationView(
    var codeText: String,
    var name: String,
    var retries: String,
    var region: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toGlobalDisputeReservationView(item: GlobalDisputeReservation): GlobalDisputeReservationView {
    return GlobalDisputeReservationView(
        codeText = item.code,
        name = truncate(item.name, 40),
        retries = item.retries.toString(),
        region = item.region,
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalDisputeReservationViews(items: MutableList<GlobalDisputeReservation>): MutableList<GlobalDisputeReservationView> {
    val views = mutableListOf<GlobalDisputeReservationView>()
    for (item in items) {
        views.add(toGlobalDisputeReservationView(item))
    }
    return views
}

fun sumGlobalDisputeReservationView(view: GlobalDisputeReservationView): Double {
    val parsedRetries = view.retries.toInt()
    return parsedRetries.toDouble()
}
