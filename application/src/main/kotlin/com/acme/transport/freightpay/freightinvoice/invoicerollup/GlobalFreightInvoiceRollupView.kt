package com.acme.transport.freightpay.freightinvoice.invoicerollup

/** How screens and exports show a global freight invoice rollup. */
data class GlobalFreightInvoiceRollupView(
    var code: String,
    var name: String,
    var trailerId: String,
    var title: String,
    var odometerText: String,
    var weight: String,
    var ceiling: String,
    var checksum: String,
    var score: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toGlobalFreightInvoiceRollupView(item: GlobalFreightInvoiceRollup): GlobalFreightInvoiceRollupView {
    return GlobalFreightInvoiceRollupView(
        code = item.code,
        name = item.name,
        trailerId = item.trailerId,
        title = item.title,
        odometerText = item.odometer.toString(),
        weight = "%.2f".format(item.weight),
        ceiling = "%.2f".format(item.ceiling),
        checksum = item.checksum,
        score = "%.2f".format(item.score),
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalFreightInvoiceRollupViews(items: MutableList<GlobalFreightInvoiceRollup>): MutableList<GlobalFreightInvoiceRollupView> {
    val views = mutableListOf<GlobalFreightInvoiceRollupView>()
    for (item in items) {
        views.add(toGlobalFreightInvoiceRollupView(item))
    }
    return views
}

fun sumGlobalFreightInvoiceRollupView(view: GlobalFreightInvoiceRollupView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedOdometer.toDouble() + parsedWeight + parsedCeiling
}
