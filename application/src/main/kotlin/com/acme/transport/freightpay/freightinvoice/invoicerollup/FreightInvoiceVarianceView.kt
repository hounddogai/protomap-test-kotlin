package com.acme.transport.freightpay.freightinvoice.invoicerollup

/** How screens and exports show a freight invoice variance. */
data class FreightInvoiceVarianceView(
    var codeText: String,
    var label: String,
    var active: String,
    var country: String,
    var ownerIdText: String,
    var amount: String,
    var statusLabel: String,
)

fun toFreightInvoiceVarianceView(item: FreightInvoiceVariance): FreightInvoiceVarianceView {
    return FreightInvoiceVarianceView(
        codeText = item.code,
        label = item.label,
        active = "${item.active}",
        country = item.country,
        ownerIdText = item.ownerId,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceVarianceViews(items: MutableList<FreightInvoiceVariance>): MutableList<FreightInvoiceVarianceView> {
    val views = mutableListOf<FreightInvoiceVarianceView>()
    for (item in items) {
        views.add(toFreightInvoiceVarianceView(item))
    }
    return views
}

fun sumFreightInvoiceVarianceView(view: FreightInvoiceVarianceView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
