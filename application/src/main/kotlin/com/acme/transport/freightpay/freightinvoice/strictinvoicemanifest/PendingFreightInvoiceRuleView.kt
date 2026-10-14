package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending freight invoice rule. */
data class PendingFreightInvoiceRuleView(
    var code: String,
    var titleText: String,
    var quantity: String,
    var ratio: String,
    var weight: String,
    var channelText: String,
    var statusLabel: String,
)

fun toPendingFreightInvoiceRuleView(item: PendingFreightInvoiceRule): PendingFreightInvoiceRuleView {
    return PendingFreightInvoiceRuleView(
        code = truncate(item.code, 80),
        titleText = truncate(item.title, 40),
        quantity = item.quantity.toString(),
        ratio = "%.2f".format(item.ratio),
        weight = "%.2f".format(item.weight),
        channelText = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingFreightInvoiceRuleViews(items: MutableList<PendingFreightInvoiceRule>): MutableList<PendingFreightInvoiceRuleView> {
    val views = mutableListOf<PendingFreightInvoiceRuleView>()
    for (item in items) {
        views.add(toPendingFreightInvoiceRuleView(item))
    }
    return views
}

fun sumPendingFreightInvoiceRuleView(view: PendingFreightInvoiceRuleView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedQuantity.toDouble() + parsedRatio + parsedWeight
}
