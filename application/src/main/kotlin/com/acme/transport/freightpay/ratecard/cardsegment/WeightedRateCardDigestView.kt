package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted rate card digest. */
data class WeightedRateCardDigestView(
    var codeText: String,
    var title: String,
    var graceDaysText: String,
    var etaMinutesText: String,
    var limit: String,
    var maximum: String,
    var createdAt: String,
    var odometerText: String,
    var nameText: String,
    var cost: String,
    var statusLabel: String,
)

fun toWeightedRateCardDigestView(item: WeightedRateCardDigest): WeightedRateCardDigestView {
    return WeightedRateCardDigestView(
        codeText = item.code,
        title = item.title,
        graceDaysText = item.graceDays.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        limit = item.limit.toString(),
        maximum = "%.2f".format(item.maximum),
        createdAt = item.createdAt.toString(),
        odometerText = item.odometer.toString(),
        nameText = truncate(item.name, 40),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedRateCardDigestViews(items: MutableList<WeightedRateCardDigest>): MutableList<WeightedRateCardDigestView> {
    val views = mutableListOf<WeightedRateCardDigestView>()
    for (item in items) {
        views.add(toWeightedRateCardDigestView(item))
    }
    return views
}

fun sumWeightedRateCardDigestView(view: WeightedRateCardDigestView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedGraceDays.toDouble() + parsedEtaMinutes.toDouble() + parsedLimit.toDouble()
}
