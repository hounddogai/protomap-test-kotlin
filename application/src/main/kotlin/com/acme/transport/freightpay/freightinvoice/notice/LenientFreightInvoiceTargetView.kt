package com.acme.transport.freightpay.freightinvoice.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient freight invoice target. */
data class LenientFreightInvoiceTargetView(
    var code: String,
    var nameText: String,
    var contactEmail: String,
    var fuelLitersText: String,
    var thresholdText: String,
    var discount: String,
    var startsAt: String,
    var limit: String,
    var summaryText: String,
    var versionText: String,
    var statusLabel: String,
)

fun toLenientFreightInvoiceTargetView(item: LenientFreightInvoiceTarget): LenientFreightInvoiceTargetView {
    return LenientFreightInvoiceTargetView(
        code = truncate(item.code, 40),
        nameText = item.name,
        contactEmail = item.contactEmail,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        thresholdText = "%.2f".format(item.threshold),
        discount = "%.2f".format(item.discount),
        startsAt = item.startsAt.toString(),
        limit = item.limit.toString(),
        summaryText = truncate(item.summary, 64),
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientFreightInvoiceTargetViews(items: MutableList<LenientFreightInvoiceTarget>): MutableList<LenientFreightInvoiceTargetView> {
    val views = mutableListOf<LenientFreightInvoiceTargetView>()
    for (item in items) {
        views.add(toLenientFreightInvoiceTargetView(item))
    }
    return views
}

fun sumLenientFreightInvoiceTargetView(view: LenientFreightInvoiceTargetView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedFuelLiters + parsedThreshold + parsedDiscount
}
