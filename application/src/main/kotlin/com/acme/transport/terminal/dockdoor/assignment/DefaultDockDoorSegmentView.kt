package com.acme.transport.terminal.dockdoor.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default dock door segment. */
data class DefaultDockDoorSegmentView(
    var code: String,
    var title: String,
    var dock: String,
    var channel: String,
    var threshold: String,
    var vehicleId: String,
    var revision: String,
    var statusLabel: String,
)

fun toDefaultDockDoorSegmentView(item: DefaultDockDoorSegment): DefaultDockDoorSegmentView {
    return DefaultDockDoorSegmentView(
        code = item.code,
        title = item.title,
        dock = truncate(item.dock, 120),
        channel = truncate(item.channel, 80),
        threshold = "%.2f".format(item.threshold),
        vehicleId = item.vehicleId,
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultDockDoorSegmentViews(items: MutableList<DefaultDockDoorSegment>): MutableList<DefaultDockDoorSegmentView> {
    val views = mutableListOf<DefaultDockDoorSegmentView>()
    for (item in items) {
        views.add(toDefaultDockDoorSegmentView(item))
    }
    return views
}

fun sumDefaultDockDoorSegmentView(view: DefaultDockDoorSegmentView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedThreshold + parsedRevision.toDouble()
}
