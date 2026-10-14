package com.acme.transport.fleetops.tire.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict tire request. */
data class StrictTireRequestView(
    var codeText: String,
    var titleText: String,
    var ceiling: String,
    var source: String,
    var country: String,
    var amount: String,
    var routeCodeText: String,
    var priority: String,
    var startsAt: String,
    var checksum: String,
    var vehicleIdText: String,
    var statusLabel: String,
)

fun toStrictTireRequestView(item: StrictTireRequest): StrictTireRequestView {
    return StrictTireRequestView(
        codeText = item.code,
        titleText = truncate(item.title, 64),
        ceiling = "%.2f".format(item.ceiling),
        source = item.source,
        country = truncate(item.country, 64),
        amount = "%.2f".format(item.amount),
        routeCodeText = item.routeCode,
        priority = item.priority.toString(),
        startsAt = item.startsAt.toString(),
        checksum = truncate(item.checksum, 80),
        vehicleIdText = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTireRequestViews(items: MutableList<StrictTireRequest>): MutableList<StrictTireRequestView> {
    val views = mutableListOf<StrictTireRequestView>()
    for (item in items) {
        views.add(toStrictTireRequestView(item))
    }
    return views
}

fun sumStrictTireRequestView(view: StrictTireRequestView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedCeiling + parsedAmount + parsedPriority.toDouble()
}
