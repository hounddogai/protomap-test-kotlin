package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a backhaul estimate. */
data class BackhaulEstimateView(
    var code: String,
    var name: String,
    var amount: String,
    var approvedText: String,
    var countryText: String,
    var summary: String,
    var attempts: String,
    var lane: String,
    var depot: String,
    var loadKg: String,
    var regionText: String,
    var statusLabel: String,
)

fun toBackhaulEstimateView(item: BackhaulEstimate): BackhaulEstimateView {
    return BackhaulEstimateView(
        code = truncate(item.code, 40),
        name = item.name,
        amount = "%.2f".format(item.amount),
        approvedText = "${item.approved}",
        countryText = item.country,
        summary = truncate(item.summary, 40),
        attempts = item.attempts.toString(),
        lane = item.lane,
        depot = truncate(item.depot, 80),
        loadKg = "%.2f".format(item.loadKg),
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulEstimateViews(items: MutableList<BackhaulEstimate>): MutableList<BackhaulEstimateView> {
    val views = mutableListOf<BackhaulEstimateView>()
    for (item in items) {
        views.add(toBackhaulEstimateView(item))
    }
    return views
}

fun sumBackhaulEstimateView(view: BackhaulEstimateView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedAttempts = view.attempts.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedAmount + parsedAttempts.toDouble() + parsedLoadKg
}
