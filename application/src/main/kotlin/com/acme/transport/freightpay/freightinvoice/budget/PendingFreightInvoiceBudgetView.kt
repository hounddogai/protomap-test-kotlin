package com.acme.transport.freightpay.freightinvoice.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending freight invoice budget. */
data class PendingFreightInvoiceBudgetView(
    var code: String,
    var title: String,
    var maximumText: String,
    var threshold: String,
    var country: String,
    var margin: String,
    var statusLabel: String,
)

fun toPendingFreightInvoiceBudgetView(item: PendingFreightInvoiceBudget): PendingFreightInvoiceBudgetView {
    return PendingFreightInvoiceBudgetView(
        code = truncate(item.code, 40),
        title = item.title,
        maximumText = "%.2f".format(item.maximum),
        threshold = "%.2f".format(item.threshold),
        country = item.country,
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingFreightInvoiceBudgetViews(items: MutableList<PendingFreightInvoiceBudget>): MutableList<PendingFreightInvoiceBudgetView> {
    val views = mutableListOf<PendingFreightInvoiceBudgetView>()
    for (item in items) {
        views.add(toPendingFreightInvoiceBudgetView(item))
    }
    return views
}

fun sumPendingFreightInvoiceBudgetView(view: PendingFreightInvoiceBudgetView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedMaximum + parsedThreshold + parsedMargin
}
