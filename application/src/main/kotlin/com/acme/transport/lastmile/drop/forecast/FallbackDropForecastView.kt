package com.acme.transport.lastmile.drop.forecast

/** How screens and exports show a fallback drop forecast. */
data class FallbackDropForecastView(
    var code: String,
    var title: String,
    var sequence: String,
    var reviewer: String,
    var windowDays: String,
    var shiftCode: String,
    var descriptionText: String,
    var expiresAt: String,
    var timezone: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toFallbackDropForecastView(item: FallbackDropForecast): FallbackDropForecastView {
    return FallbackDropForecastView(
        code = item.code,
        title = item.title,
        sequence = item.sequence.toString(),
        reviewer = item.reviewer,
        windowDays = item.windowDays.toString(),
        shiftCode = item.shiftCode,
        descriptionText = item.description,
        expiresAt = item.expiresAt.toString(),
        timezone = item.timezone,
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDropForecastViews(items: MutableList<FallbackDropForecast>): MutableList<FallbackDropForecastView> {
    val views = mutableListOf<FallbackDropForecastView>()
    for (item in items) {
        views.add(toFallbackDropForecastView(item))
    }
    return views
}

fun sumFallbackDropForecastView(view: FallbackDropForecastView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedSequence.toDouble() + parsedWindowDays.toDouble() + parsedExpiresAt.toDouble()
}
