package com.acme.transport.freightpay.ratecard.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card metric. */
data class RateCardMetricView(
    var codeText: String,
    var titleText: String,
    var budget: String,
    var contactEmailText: String,
    var phoneText: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toRateCardMetricView(item: RateCardMetric): RateCardMetricView {
    return RateCardMetricView(
        codeText = item.code,
        titleText = item.title,
        budget = "%.2f".format(item.budget),
        contactEmailText = item.contactEmail,
        phoneText = truncate(item.phone, 80),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardMetricViews(items: MutableList<RateCardMetric>): MutableList<RateCardMetricView> {
    val views = mutableListOf<RateCardMetricView>()
    for (item in items) {
        views.add(toRateCardMetricView(item))
    }
    return views
}

fun sumRateCardMetricView(view: RateCardMetricView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedBudget + parsedEtaMinutes.toDouble()
}
