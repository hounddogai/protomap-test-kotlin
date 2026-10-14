package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback carbon report budget. */
data class FallbackCarbonReportBudgetView(
    var code: String,
    var nameText: String,
    var currency: String,
    var depot: String,
    var retries: String,
    var threshold: String,
    var statusLabel: String,
)

fun toFallbackCarbonReportBudgetView(item: FallbackCarbonReportBudget): FallbackCarbonReportBudgetView {
    return FallbackCarbonReportBudgetView(
        code = item.code,
        nameText = item.name,
        currency = truncate(item.currency, 80),
        depot = truncate(item.depot, 80),
        retries = item.retries.toString(),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackCarbonReportBudgetViews(items: MutableList<FallbackCarbonReportBudget>): MutableList<FallbackCarbonReportBudgetView> {
    val views = mutableListOf<FallbackCarbonReportBudgetView>()
    for (item in items) {
        views.add(toFallbackCarbonReportBudgetView(item))
    }
    return views
}

fun sumFallbackCarbonReportBudgetView(view: FallbackCarbonReportBudgetView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedRetries.toDouble() + parsedThreshold
}
