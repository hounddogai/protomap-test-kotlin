package com.acme.transport.freightpay.freightinvoice.window

/** How screens and exports show a rolling freight invoice window. */
data class RollingFreightInvoiceWindowView(
    var code: String,
    var label: String,
    var maximumText: String,
    var thresholdText: String,
    var durationMinutes: String,
    var loadKgText: String,
    var statusLabel: String,
)

fun toRollingFreightInvoiceWindowView(item: RollingFreightInvoiceWindow): RollingFreightInvoiceWindowView {
    return RollingFreightInvoiceWindowView(
        code = item.code,
        label = item.label,
        maximumText = "%.2f".format(item.maximum),
        thresholdText = "%.2f".format(item.threshold),
        durationMinutes = item.durationMinutes.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingFreightInvoiceWindowViews(items: MutableList<RollingFreightInvoiceWindow>): MutableList<RollingFreightInvoiceWindowView> {
    val views = mutableListOf<RollingFreightInvoiceWindowView>()
    for (item in items) {
        views.add(toRollingFreightInvoiceWindowView(item))
    }
    return views
}

fun sumRollingFreightInvoiceWindowView(view: RollingFreightInvoiceWindowView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedMaximum + parsedThreshold + parsedDurationMinutes.toDouble()
}
