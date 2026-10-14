package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice baseline. */
data class FreightInvoiceBaselineView(
    var code: String,
    var label: String,
    var checksum: String,
    var endsAt: String,
    var version: String,
    var statusLabel: String,
)

fun toFreightInvoiceBaselineView(item: FreightInvoiceBaseline): FreightInvoiceBaselineView {
    return FreightInvoiceBaselineView(
        code = truncate(item.code, 64),
        label = item.label,
        checksum = item.checksum,
        endsAt = item.endsAt.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceBaselineViews(items: MutableList<FreightInvoiceBaseline>): MutableList<FreightInvoiceBaselineView> {
    val views = mutableListOf<FreightInvoiceBaselineView>()
    for (item in items) {
        views.add(toFreightInvoiceBaselineView(item))
    }
    return views
}

fun sumFreightInvoiceBaselineView(view: FreightInvoiceBaselineView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedVersion = view.version.toInt()
    return parsedEndsAt.toDouble() + parsedVersion.toDouble()
}
