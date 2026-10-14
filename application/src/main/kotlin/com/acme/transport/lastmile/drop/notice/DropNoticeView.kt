package com.acme.transport.lastmile.drop.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop notice. */
data class DropNoticeView(
    var code: String,
    var title: String,
    var lane: String,
    var threshold: String,
    var ceilingText: String,
    var source: String,
    var unit: String,
    var windowDays: String,
    var graceDays: String,
    var priority: String,
    var statusLabel: String,
)

fun toDropNoticeView(item: DropNotice): DropNoticeView {
    return DropNoticeView(
        code = item.code,
        title = item.title,
        lane = item.lane,
        threshold = "%.2f".format(item.threshold),
        ceilingText = "%.2f".format(item.ceiling),
        source = truncate(item.source, 64),
        unit = item.unit,
        windowDays = item.windowDays.toString(),
        graceDays = item.graceDays.toString(),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropNoticeViews(items: MutableList<DropNotice>): MutableList<DropNoticeView> {
    val views = mutableListOf<DropNoticeView>()
    for (item in items) {
        views.add(toDropNoticeView(item))
    }
    return views
}

fun sumDropNoticeView(view: DropNoticeView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedThreshold + parsedCeiling + parsedWindowDays.toDouble()
}
