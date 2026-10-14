package com.acme.transport.freightpay.ratecard.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped rate card schedule. */
data class CappedRateCardScheduleView(
    var code: String,
    var name: String,
    var ownerIdText: String,
    var sequence: String,
    var dockText: String,
    var maximum: String,
    var routeCodeText: String,
    var visible: String,
    var timezone: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toCappedRateCardScheduleView(item: CappedRateCardSchedule): CappedRateCardScheduleView {
    return CappedRateCardScheduleView(
        code = truncate(item.code, 120),
        name = item.name,
        ownerIdText = item.ownerId,
        sequence = item.sequence.toString(),
        dockText = item.dock,
        maximum = "%.2f".format(item.maximum),
        routeCodeText = item.routeCode,
        visible = "${item.visible}",
        timezone = truncate(item.timezone, 64),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedRateCardScheduleViews(items: MutableList<CappedRateCardSchedule>): MutableList<CappedRateCardScheduleView> {
    val views = mutableListOf<CappedRateCardScheduleView>()
    for (item in items) {
        views.add(toCappedRateCardScheduleView(item))
    }
    return views
}

fun sumCappedRateCardScheduleView(view: CappedRateCardScheduleView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedSequence.toDouble() + parsedMaximum + parsedStopCount.toDouble()
}
