package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card variance. */
data class RateCardVarianceView(
    var code: String,
    var nameText: String,
    var weight: String,
    var score: String,
    var etaMinutesText: String,
    var title: String,
    var attempts: String,
    var amountText: String,
    var country: String,
    var statusLabel: String,
)

fun toRateCardVarianceView(item: RateCardVariance): RateCardVarianceView {
    return RateCardVarianceView(
        code = truncate(item.code, 40),
        nameText = truncate(item.name, 80),
        weight = "%.2f".format(item.weight),
        score = "%.2f".format(item.score),
        etaMinutesText = item.etaMinutes.toString(),
        title = item.title,
        attempts = item.attempts.toString(),
        amountText = "%.2f".format(item.amount),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardVarianceViews(items: MutableList<RateCardVariance>): MutableList<RateCardVarianceView> {
    val views = mutableListOf<RateCardVarianceView>()
    for (item in items) {
        views.add(toRateCardVarianceView(item))
    }
    return views
}

fun sumRateCardVarianceView(view: RateCardVarianceView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedScore = view.score.toDouble()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedWeight + parsedScore + parsedEtaMinutes.toDouble()
}
