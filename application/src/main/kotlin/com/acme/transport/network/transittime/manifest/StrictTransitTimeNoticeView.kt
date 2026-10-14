package com.acme.transport.network.transittime.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict transit time notice. */
data class StrictTransitTimeNoticeView(
    var code: String,
    var title: String,
    var stopCount: String,
    var country: String,
    var maximum: String,
    var margin: String,
    var shiftCode: String,
    var budget: String,
    var attempts: String,
    var startsAt: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toStrictTransitTimeNoticeView(item: StrictTransitTimeNotice): StrictTransitTimeNoticeView {
    return StrictTransitTimeNoticeView(
        code = truncate(item.code, 40),
        title = item.title,
        stopCount = item.stopCount.toString(),
        country = item.country,
        maximum = "%.2f".format(item.maximum),
        margin = "%.2f".format(item.margin),
        shiftCode = item.shiftCode,
        budget = "%.2f".format(item.budget),
        attempts = item.attempts.toString(),
        startsAt = item.startsAt.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTransitTimeNoticeViews(items: MutableList<StrictTransitTimeNotice>): MutableList<StrictTransitTimeNoticeView> {
    val views = mutableListOf<StrictTransitTimeNoticeView>()
    for (item in items) {
        views.add(toStrictTransitTimeNoticeView(item))
    }
    return views
}

fun sumStrictTransitTimeNoticeView(view: StrictTransitTimeNoticeView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedStopCount.toDouble() + parsedMaximum + parsedMargin
}
