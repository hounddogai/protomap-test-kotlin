package com.acme.transport.crewing.swap.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled swap notice. */
data class ScheduledSwapNoticeView(
    var code: String,
    var name: String,
    var carrierText: String,
    var durationMinutesText: String,
    var floorValue: String,
    var amount: String,
    var quantityText: String,
    var maximum: String,
    var contactEmailText: String,
    var label: String,
    var statusLabel: String,
)

fun toScheduledSwapNoticeView(item: ScheduledSwapNotice): ScheduledSwapNoticeView {
    return ScheduledSwapNoticeView(
        code = truncate(item.code, 120),
        name = truncate(item.name, 120),
        carrierText = item.carrier,
        durationMinutesText = item.durationMinutes.toString(),
        floorValue = "%.2f".format(item.floorValue),
        amount = "%.2f".format(item.amount),
        quantityText = item.quantity.toString(),
        maximum = "%.2f".format(item.maximum),
        contactEmailText = truncate(item.contactEmail, 40),
        label = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledSwapNoticeViews(items: MutableList<ScheduledSwapNotice>): MutableList<ScheduledSwapNoticeView> {
    val views = mutableListOf<ScheduledSwapNoticeView>()
    for (item in items) {
        views.add(toScheduledSwapNoticeView(item))
    }
    return views
}

fun sumScheduledSwapNoticeView(view: ScheduledSwapNoticeView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedDurationMinutes.toDouble() + parsedFloorValue + parsedAmount
}
