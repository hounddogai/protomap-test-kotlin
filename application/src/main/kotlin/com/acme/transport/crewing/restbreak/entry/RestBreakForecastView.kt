package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break forecast. */
data class RestBreakForecastView(
    var codeText: String,
    var title: String,
    var budget: String,
    var attempts: String,
    var marginText: String,
    var label: String,
    var weight: String,
    var activeText: String,
    var currency: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toRestBreakForecastView(item: RestBreakForecast): RestBreakForecastView {
    return RestBreakForecastView(
        codeText = item.code,
        title = truncate(item.title, 120),
        budget = "%.2f".format(item.budget),
        attempts = item.attempts.toString(),
        marginText = "%.2f".format(item.margin),
        label = item.label,
        weight = "%.2f".format(item.weight),
        activeText = "${item.active}",
        currency = truncate(item.currency, 120),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakForecastViews(items: MutableList<RestBreakForecast>): MutableList<RestBreakForecastView> {
    val views = mutableListOf<RestBreakForecastView>()
    for (item in items) {
        views.add(toRestBreakForecastView(item))
    }
    return views
}

fun sumRestBreakForecastView(view: RestBreakForecastView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedAttempts = view.attempts.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedBudget + parsedAttempts.toDouble() + parsedMargin
}
