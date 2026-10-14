package com.acme.transport.safety.cameraevent.eventadjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict camera event cycle. */
data class StrictCameraEventCycleView(
    var code: String,
    var titleText: String,
    var ratioText: String,
    var pinned: String,
    var startsAtText: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toStrictCameraEventCycleView(item: StrictCameraEventCycle): StrictCameraEventCycleView {
    return StrictCameraEventCycleView(
        code = truncate(item.code, 120),
        titleText = item.title,
        ratioText = "%.2f".format(item.ratio),
        pinned = "${item.pinned}",
        startsAtText = item.startsAt.toString(),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictCameraEventCycleViews(items: MutableList<StrictCameraEventCycle>): MutableList<StrictCameraEventCycleView> {
    val views = mutableListOf<StrictCameraEventCycleView>()
    for (item in items) {
        views.add(toStrictCameraEventCycleView(item))
    }
    return views
}

fun sumStrictCameraEventCycleView(view: StrictCameraEventCycleView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedRatio + parsedStartsAt.toDouble() + parsedCeiling
}
