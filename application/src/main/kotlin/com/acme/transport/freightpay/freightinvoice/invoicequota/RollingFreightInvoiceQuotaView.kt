package com.acme.transport.freightpay.freightinvoice.invoicequota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling freight invoice quota. */
data class RollingFreightInvoiceQuotaView(
    var code: String,
    var name: String,
    var description: String,
    var graceDays: String,
    var weight: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toRollingFreightInvoiceQuotaView(item: RollingFreightInvoiceQuota): RollingFreightInvoiceQuotaView {
    return RollingFreightInvoiceQuotaView(
        code = truncate(item.code, 64),
        name = item.name,
        description = item.description,
        graceDays = item.graceDays.toString(),
        weight = "%.2f".format(item.weight),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingFreightInvoiceQuotaViews(items: MutableList<RollingFreightInvoiceQuota>): MutableList<RollingFreightInvoiceQuotaView> {
    val views = mutableListOf<RollingFreightInvoiceQuotaView>()
    for (item in items) {
        views.add(toRollingFreightInvoiceQuotaView(item))
    }
    return views
}

fun sumRollingFreightInvoiceQuotaView(view: RollingFreightInvoiceQuotaView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedGraceDays.toDouble() + parsedWeight + parsedExpiresAt.toDouble()
}
