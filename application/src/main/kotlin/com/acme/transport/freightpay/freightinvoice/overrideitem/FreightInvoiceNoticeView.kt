package com.acme.transport.freightpay.freightinvoice.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice notice. */
data class FreightInvoiceNoticeView(
    var code: String,
    var name: String,
    var visible: String,
    var amountText: String,
    var version: String,
    var depot: String,
    var segment: String,
    var statusLabel: String,
)

fun toFreightInvoiceNoticeView(item: FreightInvoiceNotice): FreightInvoiceNoticeView {
    return FreightInvoiceNoticeView(
        code = item.code,
        name = truncate(item.name, 40),
        visible = "${item.visible}",
        amountText = "%.2f".format(item.amount),
        version = item.version.toString(),
        depot = item.depot,
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceNoticeViews(items: MutableList<FreightInvoiceNotice>): MutableList<FreightInvoiceNoticeView> {
    val views = mutableListOf<FreightInvoiceNoticeView>()
    for (item in items) {
        views.add(toFreightInvoiceNoticeView(item))
    }
    return views
}

fun sumFreightInvoiceNoticeView(view: FreightInvoiceNoticeView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedAmount + parsedVersion.toDouble()
}
