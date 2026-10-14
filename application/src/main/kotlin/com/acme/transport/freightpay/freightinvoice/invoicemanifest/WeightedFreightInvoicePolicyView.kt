package com.acme.transport.freightpay.freightinvoice.invoicemanifest

/** How screens and exports show a weighted freight invoice policy. */
data class WeightedFreightInvoicePolicyView(
    var code: String,
    var label: String,
    var pinnedText: String,
    var expiresAt: String,
    var odometer: String,
    var updatedAt: String,
    var dock: String,
    var quantity: String,
    var statusLabel: String,
)

fun toWeightedFreightInvoicePolicyView(item: WeightedFreightInvoicePolicy): WeightedFreightInvoicePolicyView {
    return WeightedFreightInvoicePolicyView(
        code = item.code,
        label = item.label,
        pinnedText = "${item.pinned}",
        expiresAt = item.expiresAt.toString(),
        odometer = item.odometer.toString(),
        updatedAt = item.updatedAt.toString(),
        dock = item.dock,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedFreightInvoicePolicyViews(items: MutableList<WeightedFreightInvoicePolicy>): MutableList<WeightedFreightInvoicePolicyView> {
    val views = mutableListOf<WeightedFreightInvoicePolicyView>()
    for (item in items) {
        views.add(toWeightedFreightInvoicePolicyView(item))
    }
    return views
}

fun sumWeightedFreightInvoicePolicyView(view: WeightedFreightInvoicePolicyView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedExpiresAt.toDouble() + parsedOdometer.toDouble() + parsedUpdatedAt.toDouble()
}
