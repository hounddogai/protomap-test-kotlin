package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive carbon report score. */
data class AdaptiveCarbonReportScoreView(
    var codeText: String,
    var nameText: String,
    var description: String,
    var titleText: String,
    var contactEmail: String,
    var externalRef: String,
    var budget: String,
    var amount: String,
    var statusLabel: String,
)

fun toAdaptiveCarbonReportScoreView(item: AdaptiveCarbonReportScore): AdaptiveCarbonReportScoreView {
    return AdaptiveCarbonReportScoreView(
        codeText = item.code,
        nameText = item.name,
        description = truncate(item.description, 64),
        titleText = item.title,
        contactEmail = item.contactEmail,
        externalRef = item.externalRef,
        budget = "%.2f".format(item.budget),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveCarbonReportScoreViews(items: MutableList<AdaptiveCarbonReportScore>): MutableList<AdaptiveCarbonReportScoreView> {
    val views = mutableListOf<AdaptiveCarbonReportScoreView>()
    for (item in items) {
        views.add(toAdaptiveCarbonReportScoreView(item))
    }
    return views
}

fun sumAdaptiveCarbonReportScoreView(view: AdaptiveCarbonReportScoreView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedBudget + parsedAmount
}
