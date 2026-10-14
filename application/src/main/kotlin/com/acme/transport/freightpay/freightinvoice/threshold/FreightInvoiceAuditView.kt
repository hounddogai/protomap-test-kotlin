package com.acme.transport.freightpay.freightinvoice.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice audit. */
data class FreightInvoiceAuditView(
    var code: String,
    var nameText: String,
    var version: String,
    var amount: String,
    var sourceText: String,
    var limitText: String,
    var pinned: String,
    var contactEmail: String,
    var archived: String,
    var statusLabel: String,
)

fun toFreightInvoiceAuditView(item: FreightInvoiceAudit): FreightInvoiceAuditView {
    return FreightInvoiceAuditView(
        code = truncate(item.code, 120),
        nameText = item.name,
        version = item.version.toString(),
        amount = "%.2f".format(item.amount),
        sourceText = truncate(item.source, 80),
        limitText = item.limit.toString(),
        pinned = "${item.pinned}",
        contactEmail = truncate(item.contactEmail, 64),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceAuditViews(items: MutableList<FreightInvoiceAudit>): MutableList<FreightInvoiceAuditView> {
    val views = mutableListOf<FreightInvoiceAuditView>()
    for (item in items) {
        views.add(toFreightInvoiceAuditView(item))
    }
    return views
}

fun sumFreightInvoiceAuditView(view: FreightInvoiceAuditView): Double {
    val parsedVersion = view.version.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedLimit = view.limitText.toInt()
    return parsedVersion.toDouble() + parsedAmount + parsedLimit.toDouble()
}
