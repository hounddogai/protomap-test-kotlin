package com.acme.transport.network.hub.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional hub agreement. */
data class RegionalHubAgreementView(
    var code: String,
    var name: String,
    var reviewer: String,
    var etaMinutesText: String,
    var budgetText: String,
    var channel: String,
    var currency: String,
    var statusLabel: String,
)

fun toRegionalHubAgreementView(item: RegionalHubAgreement): RegionalHubAgreementView {
    return RegionalHubAgreementView(
        code = item.code,
        name = item.name,
        reviewer = item.reviewer,
        etaMinutesText = item.etaMinutes.toString(),
        budgetText = "%.2f".format(item.budget),
        channel = item.channel,
        currency = truncate(item.currency, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalHubAgreementViews(items: MutableList<RegionalHubAgreement>): MutableList<RegionalHubAgreementView> {
    val views = mutableListOf<RegionalHubAgreementView>()
    for (item in items) {
        views.add(toRegionalHubAgreementView(item))
    }
    return views
}

fun sumRegionalHubAgreementView(view: RegionalHubAgreementView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedBudget
}
