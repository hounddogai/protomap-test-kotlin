package com.acme.transport.fleetops.defect.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict defect slot. */
data class StrictDefectSlotView(
    var code: String,
    var nameText: String,
    var stopCount: String,
    var approved: String,
    var locale: String,
    var summary: String,
    var lane: String,
    var countryText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toStrictDefectSlotView(item: StrictDefectSlot): StrictDefectSlotView {
    return StrictDefectSlotView(
        code = item.code,
        nameText = truncate(item.name, 40),
        stopCount = item.stopCount.toString(),
        approved = "${item.approved}",
        locale = item.locale,
        summary = item.summary,
        lane = item.lane,
        countryText = item.country,
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDefectSlotViews(items: MutableList<StrictDefectSlot>): MutableList<StrictDefectSlotView> {
    val views = mutableListOf<StrictDefectSlotView>()
    for (item in items) {
        views.add(toStrictDefectSlotView(item))
    }
    return views
}

fun sumStrictDefectSlotView(view: StrictDefectSlotView): Double {
    val parsedStopCount = view.stopCount.toInt()
    return parsedStopCount.toDouble()
}
