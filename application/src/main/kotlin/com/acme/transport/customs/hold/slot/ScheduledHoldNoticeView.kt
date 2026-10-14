package com.acme.transport.customs.hold.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled hold notice. */
data class ScheduledHoldNoticeView(
    var code: String,
    var name: String,
    var channel: String,
    var createdAt: String,
    var margin: String,
    var windowDays: String,
    var unit: String,
    var odometerText: String,
    var reviewer: String,
    var revision: String,
    var statusLabel: String,
)

fun toScheduledHoldNoticeView(item: ScheduledHoldNotice): ScheduledHoldNoticeView {
    return ScheduledHoldNoticeView(
        code = item.code,
        name = truncate(item.name, 64),
        channel = truncate(item.channel, 40),
        createdAt = item.createdAt.toString(),
        margin = "%.2f".format(item.margin),
        windowDays = item.windowDays.toString(),
        unit = item.unit,
        odometerText = item.odometer.toString(),
        reviewer = item.reviewer,
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledHoldNoticeViews(items: MutableList<ScheduledHoldNotice>): MutableList<ScheduledHoldNoticeView> {
    val views = mutableListOf<ScheduledHoldNoticeView>()
    for (item in items) {
        views.add(toScheduledHoldNoticeView(item))
    }
    return views
}

fun sumScheduledHoldNoticeView(view: ScheduledHoldNoticeView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedCreatedAt.toDouble() + parsedMargin + parsedWindowDays.toDouble()
}
