package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled trailer exception. */
data class ScheduledTrailerExceptionView(
    var code: String,
    var label: String,
    var externalRef: String,
    var windowDaysText: String,
    var graceDaysText: String,
    var unitText: String,
    var checksum: String,
    var attemptsText: String,
    var limit: String,
    var margin: String,
    var statusLabel: String,
)

fun toScheduledTrailerExceptionView(item: ScheduledTrailerException): ScheduledTrailerExceptionView {
    return ScheduledTrailerExceptionView(
        code = item.code,
        label = item.label,
        externalRef = item.externalRef,
        windowDaysText = item.windowDays.toString(),
        graceDaysText = item.graceDays.toString(),
        unitText = truncate(item.unit, 120),
        checksum = item.checksum,
        attemptsText = item.attempts.toString(),
        limit = item.limit.toString(),
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledTrailerExceptionViews(items: MutableList<ScheduledTrailerException>): MutableList<ScheduledTrailerExceptionView> {
    val views = mutableListOf<ScheduledTrailerExceptionView>()
    for (item in items) {
        views.add(toScheduledTrailerExceptionView(item))
    }
    return views
}

fun sumScheduledTrailerExceptionView(view: ScheduledTrailerExceptionView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedWindowDays.toDouble() + parsedGraceDays.toDouble() + parsedAttempts.toDouble()
}
