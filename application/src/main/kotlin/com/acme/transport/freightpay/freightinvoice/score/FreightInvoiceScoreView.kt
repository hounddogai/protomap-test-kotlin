package com.acme.transport.freightpay.freightinvoice.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice score. */
data class FreightInvoiceScoreView(
    var codeText: String,
    var name: String,
    var budget: String,
    var approved: String,
    var score: String,
    var windowDays: String,
    var weightText: String,
    var dockText: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toFreightInvoiceScoreView(item: FreightInvoiceScore): FreightInvoiceScoreView {
    return FreightInvoiceScoreView(
        codeText = item.code,
        name = item.name,
        budget = "%.2f".format(item.budget),
        approved = "${item.approved}",
        score = "%.2f".format(item.score),
        windowDays = item.windowDays.toString(),
        weightText = "%.2f".format(item.weight),
        dockText = item.dock,
        reviewer = truncate(item.reviewer, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceScoreViews(items: MutableList<FreightInvoiceScore>): MutableList<FreightInvoiceScoreView> {
    val views = mutableListOf<FreightInvoiceScoreView>()
    for (item in items) {
        views.add(toFreightInvoiceScoreView(item))
    }
    return views
}

fun sumFreightInvoiceScoreView(view: FreightInvoiceScoreView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedScore = view.score.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedBudget + parsedScore + parsedWindowDays.toDouble()
}
