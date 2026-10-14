package com.acme.transport.terminal.dockdoor.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled dock door notice. */
data class ScheduledDockDoorNoticeView(
    var code: String,
    var title: String,
    var pinned: String,
    var trailerId: String,
    var palletsText: String,
    var windowDays: String,
    var score: String,
    var locale: String,
    var currency: String,
    var regionText: String,
    var maximum: String,
    var statusLabel: String,
)

fun toScheduledDockDoorNoticeView(item: ScheduledDockDoorNotice): ScheduledDockDoorNoticeView {
    return ScheduledDockDoorNoticeView(
        code = item.code,
        title = item.title,
        pinned = "${item.pinned}",
        trailerId = item.trailerId,
        palletsText = item.pallets.toString(),
        windowDays = item.windowDays.toString(),
        score = "%.2f".format(item.score),
        locale = item.locale,
        currency = truncate(item.currency, 80),
        regionText = truncate(item.region, 64),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledDockDoorNoticeViews(items: MutableList<ScheduledDockDoorNotice>): MutableList<ScheduledDockDoorNoticeView> {
    val views = mutableListOf<ScheduledDockDoorNoticeView>()
    for (item in items) {
        views.add(toScheduledDockDoorNoticeView(item))
    }
    return views
}

fun sumScheduledDockDoorNoticeView(view: ScheduledDockDoorNoticeView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedScore = view.score.toDouble()
    return parsedPallets.toDouble() + parsedWindowDays.toDouble() + parsedScore
}
