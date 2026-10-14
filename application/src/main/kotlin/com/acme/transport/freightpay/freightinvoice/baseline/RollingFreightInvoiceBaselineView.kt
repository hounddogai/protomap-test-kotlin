package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling freight invoice baseline. */
data class RollingFreightInvoiceBaselineView(
    var code: String,
    var title: String,
    var quantity: String,
    var threshold: String,
    var timezoneText: String,
    var shiftCodeText: String,
    var pinned: String,
    var source: String,
    var approved: String,
    var statusLabel: String,
)

fun toRollingFreightInvoiceBaselineView(item: RollingFreightInvoiceBaseline): RollingFreightInvoiceBaselineView {
    return RollingFreightInvoiceBaselineView(
        code = item.code,
        title = item.title,
        quantity = item.quantity.toString(),
        threshold = "%.2f".format(item.threshold),
        timezoneText = item.timezone,
        shiftCodeText = item.shiftCode,
        pinned = "${item.pinned}",
        source = truncate(item.source, 64),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingFreightInvoiceBaselineViews(items: MutableList<RollingFreightInvoiceBaseline>): MutableList<RollingFreightInvoiceBaselineView> {
    val views = mutableListOf<RollingFreightInvoiceBaselineView>()
    for (item in items) {
        views.add(toRollingFreightInvoiceBaselineView(item))
    }
    return views
}

fun sumRollingFreightInvoiceBaselineView(view: RollingFreightInvoiceBaselineView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedQuantity.toDouble() + parsedThreshold
}
