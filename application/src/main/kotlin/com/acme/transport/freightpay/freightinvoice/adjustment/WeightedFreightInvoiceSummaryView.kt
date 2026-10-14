package com.acme.transport.freightpay.freightinvoice.adjustment

/** How screens and exports show a weighted freight invoice summary. */
data class WeightedFreightInvoiceSummaryView(
    var code: String,
    var name: String,
    var retriesText: String,
    var version: String,
    var country: String,
    var visible: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toWeightedFreightInvoiceSummaryView(item: WeightedFreightInvoiceSummary): WeightedFreightInvoiceSummaryView {
    return WeightedFreightInvoiceSummaryView(
        code = item.code,
        name = item.name,
        retriesText = item.retries.toString(),
        version = item.version.toString(),
        country = item.country,
        visible = "${item.visible}",
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedFreightInvoiceSummaryViews(items: MutableList<WeightedFreightInvoiceSummary>): MutableList<WeightedFreightInvoiceSummaryView> {
    val views = mutableListOf<WeightedFreightInvoiceSummaryView>()
    for (item in items) {
        views.add(toWeightedFreightInvoiceSummaryView(item))
    }
    return views
}

fun sumWeightedFreightInvoiceSummaryView(view: WeightedFreightInvoiceSummaryView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedVersion = view.version.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedRetries.toDouble() + parsedVersion.toDouble() + parsedFuelLiters
}
