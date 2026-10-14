package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped capacity schedule. */
data class CappedCapacityScheduleView(
    var code: String,
    var titleText: String,
    var categoryText: String,
    var threshold: String,
    var depot: String,
    var windowDaysText: String,
    var endsAtText: String,
    var channel: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toCappedCapacityScheduleView(item: CappedCapacitySchedule): CappedCapacityScheduleView {
    return CappedCapacityScheduleView(
        code = truncate(item.code, 64),
        titleText = item.title,
        categoryText = truncate(item.category, 40),
        threshold = "%.2f".format(item.threshold),
        depot = truncate(item.depot, 64),
        windowDaysText = item.windowDays.toString(),
        endsAtText = item.endsAt.toString(),
        channel = item.channel,
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedCapacityScheduleViews(items: MutableList<CappedCapacitySchedule>): MutableList<CappedCapacityScheduleView> {
    val views = mutableListOf<CappedCapacityScheduleView>()
    for (item in items) {
        views.add(toCappedCapacityScheduleView(item))
    }
    return views
}

fun sumCappedCapacityScheduleView(view: CappedCapacityScheduleView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedThreshold + parsedWindowDays.toDouble() + parsedEndsAt.toDouble()
}
