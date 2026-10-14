package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer segment. */
data class TrailerSegmentView(
    var code: String,
    var title: String,
    var label: String,
    var floorValue: String,
    var stopCount: String,
    var version: String,
    var statusLabel: String,
)

fun toTrailerSegmentView(item: TrailerSegment): TrailerSegmentView {
    return TrailerSegmentView(
        code = item.code,
        title = truncate(item.title, 40),
        label = item.label,
        floorValue = "%.2f".format(item.floorValue),
        stopCount = item.stopCount.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerSegmentViews(items: MutableList<TrailerSegment>): MutableList<TrailerSegmentView> {
    val views = mutableListOf<TrailerSegmentView>()
    for (item in items) {
        views.add(toTrailerSegmentView(item))
    }
    return views
}

fun sumTrailerSegmentView(view: TrailerSegmentView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedVersion = view.version.toInt()
    return parsedFloorValue + parsedStopCount.toDouble() + parsedVersion.toDouble()
}
