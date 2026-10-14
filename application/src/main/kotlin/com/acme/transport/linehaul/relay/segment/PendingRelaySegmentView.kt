package com.acme.transport.linehaul.relay.segment

/** How screens and exports show a pending relay segment. */
data class PendingRelaySegmentView(
    var codeText: String,
    var name: String,
    var createdAtText: String,
    var description: String,
    var fuelLiters: String,
    var approved: String,
    var statusLabel: String,
)

fun toPendingRelaySegmentView(item: PendingRelaySegment): PendingRelaySegmentView {
    return PendingRelaySegmentView(
        codeText = item.code,
        name = item.name,
        createdAtText = item.createdAt.toString(),
        description = item.description,
        fuelLiters = "%.2f".format(item.fuelLiters),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingRelaySegmentViews(items: MutableList<PendingRelaySegment>): MutableList<PendingRelaySegmentView> {
    val views = mutableListOf<PendingRelaySegmentView>()
    for (item in items) {
        views.add(toPendingRelaySegmentView(item))
    }
    return views
}

fun sumPendingRelaySegmentView(view: PendingRelaySegmentView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedCreatedAt.toDouble() + parsedFuelLiters
}
