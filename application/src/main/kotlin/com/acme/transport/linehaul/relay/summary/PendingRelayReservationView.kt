package com.acme.transport.linehaul.relay.summary

/** How screens and exports show a pending relay reservation. */
data class PendingRelayReservationView(
    var code: String,
    var title: String,
    var versionText: String,
    var updatedAtText: String,
    var maximum: String,
    var etaMinutes: String,
    var durationMinutes: String,
    var limit: String,
    var reviewer: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toPendingRelayReservationView(item: PendingRelayReservation): PendingRelayReservationView {
    return PendingRelayReservationView(
        code = item.code,
        title = item.title,
        versionText = item.version.toString(),
        updatedAtText = item.updatedAt.toString(),
        maximum = "%.2f".format(item.maximum),
        etaMinutes = item.etaMinutes.toString(),
        durationMinutes = item.durationMinutes.toString(),
        limit = item.limit.toString(),
        reviewer = item.reviewer,
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingRelayReservationViews(items: MutableList<PendingRelayReservation>): MutableList<PendingRelayReservationView> {
    val views = mutableListOf<PendingRelayReservationView>()
    for (item in items) {
        views.add(toPendingRelayReservationView(item))
    }
    return views
}

fun sumPendingRelayReservationView(view: PendingRelayReservationView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedVersion.toDouble() + parsedUpdatedAt.toDouble() + parsedMaximum
}
