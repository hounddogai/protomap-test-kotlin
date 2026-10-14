package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled zone segment. */
data class ScheduledZoneSegmentView(
    var codeText: String,
    var title: String,
    var pinned: String,
    var channel: String,
    var lane: String,
    var quantity: String,
    var statusLabel: String,
)

fun toScheduledZoneSegmentView(item: ScheduledZoneSegment): ScheduledZoneSegmentView {
    return ScheduledZoneSegmentView(
        codeText = truncate(item.code, 80),
        title = truncate(item.title, 64),
        pinned = "${item.pinned}",
        channel = item.channel,
        lane = item.lane,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledZoneSegmentViews(items: MutableList<ScheduledZoneSegment>): MutableList<ScheduledZoneSegmentView> {
    val views = mutableListOf<ScheduledZoneSegmentView>()
    for (item in items) {
        views.add(toScheduledZoneSegmentView(item))
    }
    return views
}

fun sumScheduledZoneSegmentView(view: ScheduledZoneSegmentView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
