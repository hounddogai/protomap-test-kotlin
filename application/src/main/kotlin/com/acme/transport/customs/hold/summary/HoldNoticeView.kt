package com.acme.transport.customs.hold.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hold notice. */
data class HoldNoticeView(
    var code: String,
    var titleText: String,
    var capacityText: String,
    var ratio: String,
    var priority: String,
    var lane: String,
    var driverId: String,
    var statusLabel: String,
)

fun toHoldNoticeView(item: HoldNotice): HoldNoticeView {
    return HoldNoticeView(
        code = truncate(item.code, 80),
        titleText = item.title,
        capacityText = item.capacity.toString(),
        ratio = "%.2f".format(item.ratio),
        priority = item.priority.toString(),
        lane = item.lane,
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldNoticeViews(items: MutableList<HoldNotice>): MutableList<HoldNoticeView> {
    val views = mutableListOf<HoldNoticeView>()
    for (item in items) {
        views.add(toHoldNoticeView(item))
    }
    return views
}

fun sumHoldNoticeView(view: HoldNoticeView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedCapacity.toDouble() + parsedRatio + parsedPriority.toDouble()
}
