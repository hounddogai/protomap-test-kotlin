package com.acme.transport.freightpay.freightinvoice.threshold

/** How screens and exports show a regional freight invoice threshold. */
data class RegionalFreightInvoiceThresholdView(
    var code: String,
    var title: String,
    var label: String,
    var weight: String,
    var maximum: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toRegionalFreightInvoiceThresholdView(item: RegionalFreightInvoiceThreshold): RegionalFreightInvoiceThresholdView {
    return RegionalFreightInvoiceThresholdView(
        code = item.code,
        title = item.title,
        label = item.label,
        weight = "%.2f".format(item.weight),
        maximum = "%.2f".format(item.maximum),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalFreightInvoiceThresholdViews(items: MutableList<RegionalFreightInvoiceThreshold>): MutableList<RegionalFreightInvoiceThresholdView> {
    val views = mutableListOf<RegionalFreightInvoiceThresholdView>()
    for (item in items) {
        views.add(toRegionalFreightInvoiceThresholdView(item))
    }
    return views
}

fun sumRegionalFreightInvoiceThresholdView(view: RegionalFreightInvoiceThresholdView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedWeight + parsedMaximum + parsedLoadKg
}
