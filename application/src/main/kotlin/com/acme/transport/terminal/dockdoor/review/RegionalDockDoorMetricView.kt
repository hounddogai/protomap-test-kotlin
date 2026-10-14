package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional dock door metric. */
data class RegionalDockDoorMetricView(
    var codeText: String,
    var title: String,
    var attempts: String,
    var timezone: String,
    var budget: String,
    var amount: String,
    var country: String,
    var statusLabel: String,
)

fun toRegionalDockDoorMetricView(item: RegionalDockDoorMetric): RegionalDockDoorMetricView {
    return RegionalDockDoorMetricView(
        codeText = item.code,
        title = item.title,
        attempts = item.attempts.toString(),
        timezone = item.timezone,
        budget = "%.2f".format(item.budget),
        amount = "%.2f".format(item.amount),
        country = truncate(item.country, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDockDoorMetricViews(items: MutableList<RegionalDockDoorMetric>): MutableList<RegionalDockDoorMetricView> {
    val views = mutableListOf<RegionalDockDoorMetricView>()
    for (item in items) {
        views.add(toRegionalDockDoorMetricView(item))
    }
    return views
}

fun sumRegionalDockDoorMetricView(view: RegionalDockDoorMetricView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedAttempts.toDouble() + parsedBudget + parsedAmount
}
