package com.acme.transport.freightpay.ratecard.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled rate card baseline. */
data class ScheduledRateCardBaselineView(
    var codeText: String,
    var title: String,
    var externalRef: String,
    var country: String,
    var channel: String,
    var windowDaysText: String,
    var rate: String,
    var weight: String,
    var shiftCodeText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toScheduledRateCardBaselineView(item: ScheduledRateCardBaseline): ScheduledRateCardBaselineView {
    return ScheduledRateCardBaselineView(
        codeText = item.code,
        title = item.title,
        externalRef = truncate(item.externalRef, 64),
        country = item.country,
        channel = truncate(item.channel, 64),
        windowDaysText = item.windowDays.toString(),
        rate = "%.2f".format(item.rate),
        weight = "%.2f".format(item.weight),
        shiftCodeText = item.shiftCode,
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledRateCardBaselineViews(items: MutableList<ScheduledRateCardBaseline>): MutableList<ScheduledRateCardBaselineView> {
    val views = mutableListOf<ScheduledRateCardBaselineView>()
    for (item in items) {
        views.add(toScheduledRateCardBaselineView(item))
    }
    return views
}

fun sumScheduledRateCardBaselineView(view: ScheduledRateCardBaselineView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedWindowDays.toDouble() + parsedRate + parsedWeight
}
