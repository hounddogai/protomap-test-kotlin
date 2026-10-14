package com.acme.transport.safety.scorecard.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending scorecard forecast. */
data class PendingScorecardForecastView(
    var code: String,
    var name: String,
    var windowDaysText: String,
    var contactEmailText: String,
    var shiftCode: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toPendingScorecardForecastView(item: PendingScorecardForecast): PendingScorecardForecastView {
    return PendingScorecardForecastView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 64),
        windowDaysText = item.windowDays.toString(),
        contactEmailText = truncate(item.contactEmail, 40),
        shiftCode = item.shiftCode,
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingScorecardForecastViews(items: MutableList<PendingScorecardForecast>): MutableList<PendingScorecardForecastView> {
    val views = mutableListOf<PendingScorecardForecastView>()
    for (item in items) {
        views.add(toPendingScorecardForecastView(item))
    }
    return views
}

fun sumPendingScorecardForecastView(view: PendingScorecardForecastView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedWindowDays.toDouble() + parsedExpiresAt.toDouble()
}
