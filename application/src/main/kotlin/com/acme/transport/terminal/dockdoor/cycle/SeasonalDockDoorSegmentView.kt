package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal dock door segment. */
data class SeasonalDockDoorSegmentView(
    var code: String,
    var title: String,
    var regionText: String,
    var name: String,
    var floorValue: String,
    var trailerIdText: String,
    var endsAtText: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toSeasonalDockDoorSegmentView(item: SeasonalDockDoorSegment): SeasonalDockDoorSegmentView {
    return SeasonalDockDoorSegmentView(
        code = truncate(item.code, 40),
        title = item.title,
        regionText = item.region,
        name = item.name,
        floorValue = "%.2f".format(item.floorValue),
        trailerIdText = item.trailerId,
        endsAtText = item.endsAt.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDockDoorSegmentViews(items: MutableList<SeasonalDockDoorSegment>): MutableList<SeasonalDockDoorSegmentView> {
    val views = mutableListOf<SeasonalDockDoorSegmentView>()
    for (item in items) {
        views.add(toSeasonalDockDoorSegmentView(item))
    }
    return views
}

fun sumSeasonalDockDoorSegmentView(view: SeasonalDockDoorSegmentView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedFloorValue + parsedEndsAt.toDouble() + parsedWindowDays.toDouble()
}
