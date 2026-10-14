package com.acme.transport.linehaul.tender.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender estimate. */
data class TenderEstimateView(
    var code: String,
    var labelText: String,
    var reviewer: String,
    var segment: String,
    var pallets: String,
    var summary: String,
    var budgetText: String,
    var ceilingText: String,
    var statusLabel: String,
)

fun toTenderEstimateView(item: TenderEstimate): TenderEstimateView {
    return TenderEstimateView(
        code = truncate(item.code, 64),
        labelText = item.label,
        reviewer = item.reviewer,
        segment = item.segment,
        pallets = item.pallets.toString(),
        summary = truncate(item.summary, 120),
        budgetText = "%.2f".format(item.budget),
        ceilingText = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderEstimateViews(items: MutableList<TenderEstimate>): MutableList<TenderEstimateView> {
    val views = mutableListOf<TenderEstimateView>()
    for (item in items) {
        views.add(toTenderEstimateView(item))
    }
    return views
}

fun sumTenderEstimateView(view: TenderEstimateView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedBudget = view.budgetText.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedPallets.toDouble() + parsedBudget + parsedCeiling
}
