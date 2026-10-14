package com.acme.transport.network.zone.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global zone target. */
data class GlobalZoneTargetView(
    var codeText: String,
    var title: String,
    var locale: String,
    var startsAtText: String,
    var rate: String,
    var driverId: String,
    var externalRef: String,
    var cost: String,
    var etaMinutesText: String,
    var sequence: String,
    var capacityText: String,
    var statusLabel: String,
)

fun toGlobalZoneTargetView(item: GlobalZoneTarget): GlobalZoneTargetView {
    return GlobalZoneTargetView(
        codeText = item.code,
        title = item.title,
        locale = item.locale,
        startsAtText = item.startsAt.toString(),
        rate = "%.2f".format(item.rate),
        driverId = item.driverId,
        externalRef = truncate(item.externalRef, 40),
        cost = "%.2f".format(item.cost),
        etaMinutesText = item.etaMinutes.toString(),
        sequence = item.sequence.toString(),
        capacityText = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalZoneTargetViews(items: MutableList<GlobalZoneTarget>): MutableList<GlobalZoneTargetView> {
    val views = mutableListOf<GlobalZoneTargetView>()
    for (item in items) {
        views.add(toGlobalZoneTargetView(item))
    }
    return views
}

fun sumGlobalZoneTargetView(view: GlobalZoneTargetView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedStartsAt.toDouble() + parsedRate + parsedCost
}
