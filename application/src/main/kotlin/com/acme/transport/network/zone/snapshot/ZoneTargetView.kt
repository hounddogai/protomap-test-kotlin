package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a zone target. */
data class ZoneTargetView(
    var code: String,
    var titleText: String,
    var ownerIdText: String,
    var active: String,
    var durationMinutesText: String,
    var startsAt: String,
    var ceilingText: String,
    var discountText: String,
    var dock: String,
    var carrierText: String,
    var statusLabel: String,
)

fun toZoneTargetView(item: ZoneTarget): ZoneTargetView {
    return ZoneTargetView(
        code = item.code,
        titleText = truncate(item.title, 64),
        ownerIdText = truncate(item.ownerId, 40),
        active = "${item.active}",
        durationMinutesText = item.durationMinutes.toString(),
        startsAt = item.startsAt.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        discountText = "%.2f".format(item.discount),
        dock = item.dock,
        carrierText = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneTargetViews(items: MutableList<ZoneTarget>): MutableList<ZoneTargetView> {
    val views = mutableListOf<ZoneTargetView>()
    for (item in items) {
        views.add(toZoneTargetView(item))
    }
    return views
}

fun sumZoneTargetView(view: ZoneTargetView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedDurationMinutes.toDouble() + parsedStartsAt.toDouble() + parsedCeiling
}
