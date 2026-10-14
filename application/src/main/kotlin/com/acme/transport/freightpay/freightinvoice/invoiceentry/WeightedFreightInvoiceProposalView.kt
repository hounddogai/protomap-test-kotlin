package com.acme.transport.freightpay.freightinvoice.invoiceentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted freight invoice proposal. */
data class WeightedFreightInvoiceProposalView(
    var code: String,
    var titleText: String,
    var routeCode: String,
    var palletsText: String,
    var checksum: String,
    var quantity: String,
    var rate: String,
    var version: String,
    var channel: String,
    var windowDaysText: String,
    var statusLabel: String,
)

fun toWeightedFreightInvoiceProposalView(item: WeightedFreightInvoiceProposal): WeightedFreightInvoiceProposalView {
    return WeightedFreightInvoiceProposalView(
        code = truncate(item.code, 40),
        titleText = truncate(item.title, 120),
        routeCode = item.routeCode,
        palletsText = item.pallets.toString(),
        checksum = item.checksum,
        quantity = item.quantity.toString(),
        rate = "%.2f".format(item.rate),
        version = item.version.toString(),
        channel = truncate(item.channel, 64),
        windowDaysText = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedFreightInvoiceProposalViews(items: MutableList<WeightedFreightInvoiceProposal>): MutableList<WeightedFreightInvoiceProposalView> {
    val views = mutableListOf<WeightedFreightInvoiceProposalView>()
    for (item in items) {
        views.add(toWeightedFreightInvoiceProposalView(item))
    }
    return views
}

fun sumWeightedFreightInvoiceProposalView(view: WeightedFreightInvoiceProposalView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedPallets.toDouble() + parsedQuantity.toDouble() + parsedRate
}
