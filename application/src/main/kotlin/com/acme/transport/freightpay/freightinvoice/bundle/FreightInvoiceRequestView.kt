package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice request. */
data class FreightInvoiceRequestView(
    var code: String,
    var titleText: String,
    var summary: String,
    var revisionText: String,
    var notes: String,
    var statusLabel: String,
)

fun toFreightInvoiceRequestView(item: FreightInvoiceRequest): FreightInvoiceRequestView {
    return FreightInvoiceRequestView(
        code = truncate(item.code, 80),
        titleText = item.title,
        summary = item.summary,
        revisionText = item.revision.toString(),
        notes = truncate(item.notes, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceRequestViews(items: MutableList<FreightInvoiceRequest>): MutableList<FreightInvoiceRequestView> {
    val views = mutableListOf<FreightInvoiceRequestView>()
    for (item in items) {
        views.add(toFreightInvoiceRequestView(item))
    }
    return views
}

fun sumFreightInvoiceRequestView(view: FreightInvoiceRequestView): Double {
    val parsedRevision = view.revisionText.toInt()
    return parsedRevision.toDouble()
}
