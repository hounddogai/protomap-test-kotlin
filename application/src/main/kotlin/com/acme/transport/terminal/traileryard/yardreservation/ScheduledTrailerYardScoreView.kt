package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled trailer yard score. */
data class ScheduledTrailerYardScoreView(
    var code: String,
    var nameText: String,
    var phone: String,
    var timezone: String,
    var country: String,
    var windowDays: String,
    var vehicleId: String,
    var amount: String,
    var statusLabel: String,
)

fun toScheduledTrailerYardScoreView(item: ScheduledTrailerYardScore): ScheduledTrailerYardScoreView {
    return ScheduledTrailerYardScoreView(
        code = item.code,
        nameText = truncate(item.name, 64),
        phone = truncate(item.phone, 80),
        timezone = item.timezone,
        country = truncate(item.country, 64),
        windowDays = item.windowDays.toString(),
        vehicleId = item.vehicleId,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledTrailerYardScoreViews(items: MutableList<ScheduledTrailerYardScore>): MutableList<ScheduledTrailerYardScoreView> {
    val views = mutableListOf<ScheduledTrailerYardScoreView>()
    for (item in items) {
        views.add(toScheduledTrailerYardScoreView(item))
    }
    return views
}

fun sumScheduledTrailerYardScoreView(view: ScheduledTrailerYardScoreView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedWindowDays.toDouble() + parsedAmount
}
