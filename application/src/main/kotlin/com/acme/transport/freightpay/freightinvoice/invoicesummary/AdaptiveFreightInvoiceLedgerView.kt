package com.acme.transport.freightpay.freightinvoice.invoicesummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive freight invoice ledger. */
data class AdaptiveFreightInvoiceLedgerView(
    var code: String,
    var titleText: String,
    var minimum: String,
    var margin: String,
    var maximumText: String,
    var approved: String,
    var odometerText: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toAdaptiveFreightInvoiceLedgerView(item: AdaptiveFreightInvoiceLedger): AdaptiveFreightInvoiceLedgerView {
    return AdaptiveFreightInvoiceLedgerView(
        code = item.code,
        titleText = truncate(item.title, 40),
        minimum = "%.2f".format(item.minimum),
        margin = "%.2f".format(item.margin),
        maximumText = "%.2f".format(item.maximum),
        approved = "${item.approved}",
        odometerText = item.odometer.toString(),
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveFreightInvoiceLedgerViews(items: MutableList<AdaptiveFreightInvoiceLedger>): MutableList<AdaptiveFreightInvoiceLedgerView> {
    val views = mutableListOf<AdaptiveFreightInvoiceLedgerView>()
    for (item in items) {
        views.add(toAdaptiveFreightInvoiceLedgerView(item))
    }
    return views
}

fun sumAdaptiveFreightInvoiceLedgerView(view: AdaptiveFreightInvoiceLedgerView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedMinimum + parsedMargin + parsedMaximum
}
