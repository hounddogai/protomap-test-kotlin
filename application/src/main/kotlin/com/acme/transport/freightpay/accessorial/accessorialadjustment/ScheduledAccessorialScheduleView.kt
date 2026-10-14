package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled accessorial schedule. */
data class ScheduledAccessorialScheduleView(
    var code: String,
    var title: String,
    var laneText: String,
    var floorValue: String,
    var visible: String,
    var rate: String,
    var windowDays: String,
    var discountText: String,
    var dock: String,
    var statusLabel: String,
)

fun toScheduledAccessorialScheduleView(item: ScheduledAccessorialSchedule): ScheduledAccessorialScheduleView {
    return ScheduledAccessorialScheduleView(
        code = item.code,
        title = item.title,
        laneText = item.lane,
        floorValue = "%.2f".format(item.floorValue),
        visible = "${item.visible}",
        rate = "%.2f".format(item.rate),
        windowDays = item.windowDays.toString(),
        discountText = "%.2f".format(item.discount),
        dock = truncate(item.dock, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledAccessorialScheduleViews(items: MutableList<ScheduledAccessorialSchedule>): MutableList<ScheduledAccessorialScheduleView> {
    val views = mutableListOf<ScheduledAccessorialScheduleView>()
    for (item in items) {
        views.add(toScheduledAccessorialScheduleView(item))
    }
    return views
}

fun sumScheduledAccessorialScheduleView(view: ScheduledAccessorialScheduleView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedRate = view.rate.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedFloorValue + parsedRate + parsedWindowDays.toDouble()
}
