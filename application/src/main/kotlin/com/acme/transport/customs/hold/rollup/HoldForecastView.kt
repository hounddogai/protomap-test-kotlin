package com.acme.transport.customs.hold.rollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a hold forecast. */
data class HoldForecastView(
    var codeText: String,
    var name: String,
    var score: String,
    var externalRefText: String,
    var currency: String,
    var marginText: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toHoldForecastView(item: HoldForecast): HoldForecastView {
    return HoldForecastView(
        codeText = item.code,
        name = item.name,
        score = "%.2f".format(item.score),
        externalRefText = item.externalRef,
        currency = truncate(item.currency, 40),
        marginText = "%.2f".format(item.margin),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldForecastViews(items: MutableList<HoldForecast>): MutableList<HoldForecastView> {
    val views = mutableListOf<HoldForecastView>()
    for (item in items) {
        views.add(toHoldForecastView(item))
    }
    return views
}

fun sumHoldForecastView(view: HoldForecastView): Double {
    val parsedScore = view.score.toDouble()
    val parsedMargin = view.marginText.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedScore + parsedMargin + parsedStartsAt.toDouble()
}
