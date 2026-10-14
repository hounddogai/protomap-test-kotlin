package com.acme.transport.linehaul.capacity.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global capacity reservation. */
data class GlobalCapacityReservationView(
    var code: String,
    var name: String,
    var attempts: String,
    var region: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toGlobalCapacityReservationView(item: GlobalCapacityReservation): GlobalCapacityReservationView {
    return GlobalCapacityReservationView(
        code = item.code,
        name = item.name,
        attempts = item.attempts.toString(),
        region = truncate(item.region, 80),
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalCapacityReservationViews(items: MutableList<GlobalCapacityReservation>): MutableList<GlobalCapacityReservationView> {
    val views = mutableListOf<GlobalCapacityReservationView>()
    for (item in items) {
        views.add(toGlobalCapacityReservationView(item))
    }
    return views
}

fun sumGlobalCapacityReservationView(view: GlobalCapacityReservationView): Double {
    val parsedAttempts = view.attempts.toInt()
    return parsedAttempts.toDouble()
}
