package com.acme.transport.linehaul.tender.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender summary. */
data class TenderSummaryView(
    var codeText: String,
    var title: String,
    var score: String,
    var locale: String,
    var budget: String,
    var depot: String,
    var nameText: String,
    var unit: String,
    var statusLabel: String,
)

fun toTenderSummaryView(item: TenderSummary): TenderSummaryView {
    return TenderSummaryView(
        codeText = truncate(item.code, 80),
        title = truncate(item.title, 64),
        score = "%.2f".format(item.score),
        locale = item.locale,
        budget = "%.2f".format(item.budget),
        depot = item.depot,
        nameText = item.name,
        unit = truncate(item.unit, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderSummaryViews(items: MutableList<TenderSummary>): MutableList<TenderSummaryView> {
    val views = mutableListOf<TenderSummaryView>()
    for (item in items) {
        views.add(toTenderSummaryView(item))
    }
    return views
}

fun sumTenderSummaryView(view: TenderSummaryView): Double {
    val parsedScore = view.score.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedScore + parsedBudget
}
