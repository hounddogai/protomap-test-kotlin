package com.acme.transport.crewing.availability.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability notice. */
data class AvailabilityNoticeView(
    var code: String,
    var name: String,
    var summaryText: String,
    var windowDaysText: String,
    var maximum: String,
    var graceDays: String,
    var vehicleIdText: String,
    var durationMinutes: String,
    var carrier: String,
    var description: String,
    var discount: String,
    var statusLabel: String,
)

fun toAvailabilityNoticeView(item: AvailabilityNotice): AvailabilityNoticeView {
    return AvailabilityNoticeView(
        code = item.code,
        name = truncate(item.name, 40),
        summaryText = item.summary,
        windowDaysText = item.windowDays.toString(),
        maximum = "%.2f".format(item.maximum),
        graceDays = item.graceDays.toString(),
        vehicleIdText = truncate(item.vehicleId, 80),
        durationMinutes = item.durationMinutes.toString(),
        carrier = item.carrier,
        description = item.description,
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityNoticeViews(items: MutableList<AvailabilityNotice>): MutableList<AvailabilityNoticeView> {
    val views = mutableListOf<AvailabilityNoticeView>()
    for (item in items) {
        views.add(toAvailabilityNoticeView(item))
    }
    return views
}

fun sumAvailabilityNoticeView(view: AvailabilityNoticeView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedWindowDays.toDouble() + parsedMaximum + parsedGraceDays.toDouble()
}
