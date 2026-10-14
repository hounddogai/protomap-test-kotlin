package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a accessorial schedule. */
data class AccessorialScheduleView(
    var codeText: String,
    var label: String,
    var floorValueText: String,
    var createdAt: String,
    var visibleText: String,
    var lane: String,
    var nameText: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toAccessorialScheduleView(item: AccessorialSchedule): AccessorialScheduleView {
    return AccessorialScheduleView(
        codeText = truncate(item.code, 80),
        label = item.label,
        floorValueText = "%.2f".format(item.floorValue),
        createdAt = item.createdAt.toString(),
        visibleText = "${item.visible}",
        lane = item.lane,
        nameText = item.name,
        summaryText = truncate(item.summary, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialScheduleViews(items: MutableList<AccessorialSchedule>): MutableList<AccessorialScheduleView> {
    val views = mutableListOf<AccessorialScheduleView>()
    for (item in items) {
        views.add(toAccessorialScheduleView(item))
    }
    return views
}

fun sumAccessorialScheduleView(view: AccessorialScheduleView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedFloorValue + parsedCreatedAt.toDouble()
}
