package com.acme.transport.freightpay.ratecard.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card summary. */
data class RateCardSummaryView(
    var code: String,
    var nameText: String,
    var summary: String,
    var attempts: String,
    var locale: String,
    var odometer: String,
    var statusLabel: String,
)

fun toRateCardSummaryView(item: RateCardSummary): RateCardSummaryView {
    return RateCardSummaryView(
        code = truncate(item.code, 120),
        nameText = item.name,
        summary = item.summary,
        attempts = item.attempts.toString(),
        locale = truncate(item.locale, 40),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardSummaryViews(items: MutableList<RateCardSummary>): MutableList<RateCardSummaryView> {
    val views = mutableListOf<RateCardSummaryView>()
    for (item in items) {
        views.add(toRateCardSummaryView(item))
    }
    return views
}

fun sumRateCardSummaryView(view: RateCardSummaryView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedAttempts.toDouble() + parsedOdometer.toDouble()
}
