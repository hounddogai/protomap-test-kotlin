package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card preference. */
data class RateCardPreferenceView(
    var code: String,
    var label: String,
    var threshold: String,
    var lane: String,
    var score: String,
    var statusLabel: String,
)

fun toRateCardPreferenceView(item: RateCardPreference): RateCardPreferenceView {
    return RateCardPreferenceView(
        code = item.code,
        label = truncate(item.label, 40),
        threshold = "%.2f".format(item.threshold),
        lane = item.lane,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardPreferenceViews(items: MutableList<RateCardPreference>): MutableList<RateCardPreferenceView> {
    val views = mutableListOf<RateCardPreferenceView>()
    for (item in items) {
        views.add(toRateCardPreferenceView(item))
    }
    return views
}

fun sumRateCardPreferenceView(view: RateCardPreferenceView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedScore = view.score.toDouble()
    return parsedThreshold + parsedScore
}
