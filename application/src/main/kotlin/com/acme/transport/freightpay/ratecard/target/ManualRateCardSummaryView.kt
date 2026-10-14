package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual rate card summary. */
data class ManualRateCardSummaryView(
    var code: String,
    var label: String,
    var depot: String,
    var rate: String,
    var budgetText: String,
    var statusLabel: String,
)

fun toManualRateCardSummaryView(item: ManualRateCardSummary): ManualRateCardSummaryView {
    return ManualRateCardSummaryView(
        code = item.code,
        label = truncate(item.label, 120),
        depot = truncate(item.depot, 64),
        rate = "%.2f".format(item.rate),
        budgetText = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualRateCardSummaryViews(items: MutableList<ManualRateCardSummary>): MutableList<ManualRateCardSummaryView> {
    val views = mutableListOf<ManualRateCardSummaryView>()
    for (item in items) {
        views.add(toManualRateCardSummaryView(item))
    }
    return views
}

fun sumManualRateCardSummaryView(view: ManualRateCardSummaryView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedBudget = view.budgetText.toDouble()
    return parsedRate + parsedBudget
}
