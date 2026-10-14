package com.acme.transport.terminal.sortation.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending sortation segment. */
data class PendingSortationSegmentView(
    var code: String,
    var name: String,
    var descriptionText: String,
    var priority: String,
    var windowDaysText: String,
    var floorValueText: String,
    var externalRefText: String,
    var expiresAtText: String,
    var laneText: String,
    var score: String,
    var statusLabel: String,
)

fun toPendingSortationSegmentView(item: PendingSortationSegment): PendingSortationSegmentView {
    return PendingSortationSegmentView(
        code = item.code,
        name = truncate(item.name, 64),
        descriptionText = item.description,
        priority = item.priority.toString(),
        windowDaysText = item.windowDays.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        externalRefText = truncate(item.externalRef, 64),
        expiresAtText = item.expiresAt.toString(),
        laneText = truncate(item.lane, 40),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingSortationSegmentViews(items: MutableList<PendingSortationSegment>): MutableList<PendingSortationSegmentView> {
    val views = mutableListOf<PendingSortationSegmentView>()
    for (item in items) {
        views.add(toPendingSortationSegmentView(item))
    }
    return views
}

fun sumPendingSortationSegmentView(view: PendingSortationSegmentView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedPriority.toDouble() + parsedWindowDays.toDouble() + parsedFloorValue
}
