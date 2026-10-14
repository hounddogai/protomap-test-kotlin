package com.acme.transport.network.transittime.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a transit time segment. */
data class TransitTimeSegmentView(
    var code: String,
    var name: String,
    var maximumText: String,
    var channel: String,
    var attempts: String,
    var statusLabel: String,
)

fun toTransitTimeSegmentView(item: TransitTimeSegment): TransitTimeSegmentView {
    return TransitTimeSegmentView(
        code = item.code,
        name = item.name,
        maximumText = "%.2f".format(item.maximum),
        channel = truncate(item.channel, 64),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeSegmentViews(items: MutableList<TransitTimeSegment>): MutableList<TransitTimeSegmentView> {
    val views = mutableListOf<TransitTimeSegmentView>()
    for (item in items) {
        views.add(toTransitTimeSegmentView(item))
    }
    return views
}

fun sumTransitTimeSegmentView(view: TransitTimeSegmentView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedMaximum + parsedAttempts.toDouble()
}
