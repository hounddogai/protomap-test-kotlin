package com.acme.transport.emissions.idling.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default idling segment. */
data class DefaultIdlingSegmentView(
    var code: String,
    var title: String,
    var margin: String,
    var windowDays: String,
    var dock: String,
    var timezone: String,
    var checksum: String,
    var priorityText: String,
    var palletsText: String,
    var statusLabel: String,
)

fun toDefaultIdlingSegmentView(item: DefaultIdlingSegment): DefaultIdlingSegmentView {
    return DefaultIdlingSegmentView(
        code = truncate(item.code, 64),
        title = item.title,
        margin = "%.2f".format(item.margin),
        windowDays = item.windowDays.toString(),
        dock = item.dock,
        timezone = item.timezone,
        checksum = item.checksum,
        priorityText = item.priority.toString(),
        palletsText = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultIdlingSegmentViews(items: MutableList<DefaultIdlingSegment>): MutableList<DefaultIdlingSegmentView> {
    val views = mutableListOf<DefaultIdlingSegmentView>()
    for (item in items) {
        views.add(toDefaultIdlingSegmentView(item))
    }
    return views
}

fun sumDefaultIdlingSegmentView(view: DefaultIdlingSegmentView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedPriority = view.priorityText.toInt()
    return parsedMargin + parsedWindowDays.toDouble() + parsedPriority.toDouble()
}
