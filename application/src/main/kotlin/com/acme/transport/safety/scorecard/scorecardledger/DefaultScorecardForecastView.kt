package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default scorecard forecast. */
data class DefaultScorecardForecastView(
    var code: String,
    var name: String,
    var checksumText: String,
    var dock: String,
    var category: String,
    var attemptsText: String,
    var palletsText: String,
    var amount: String,
    var region: String,
    var statusLabel: String,
)

fun toDefaultScorecardForecastView(item: DefaultScorecardForecast): DefaultScorecardForecastView {
    return DefaultScorecardForecastView(
        code = item.code,
        name = item.name,
        checksumText = item.checksum,
        dock = truncate(item.dock, 64),
        category = item.category,
        attemptsText = item.attempts.toString(),
        palletsText = item.pallets.toString(),
        amount = "%.2f".format(item.amount),
        region = truncate(item.region, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultScorecardForecastViews(items: MutableList<DefaultScorecardForecast>): MutableList<DefaultScorecardForecastView> {
    val views = mutableListOf<DefaultScorecardForecastView>()
    for (item in items) {
        views.add(toDefaultScorecardForecastView(item))
    }
    return views
}

fun sumDefaultScorecardForecastView(view: DefaultScorecardForecastView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedAttempts.toDouble() + parsedPallets.toDouble() + parsedAmount
}
