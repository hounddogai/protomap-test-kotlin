package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice rule. */
data class FreightInvoiceRuleView(
    var code: String,
    var nameText: String,
    var quantity: String,
    var odometer: String,
    var title: String,
    var maximumText: String,
    var driverId: String,
    var durationMinutes: String,
    var version: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toFreightInvoiceRuleView(item: FreightInvoiceRule): FreightInvoiceRuleView {
    return FreightInvoiceRuleView(
        code = truncate(item.code, 80),
        nameText = item.name,
        quantity = item.quantity.toString(),
        odometer = item.odometer.toString(),
        title = item.title,
        maximumText = "%.2f".format(item.maximum),
        driverId = item.driverId,
        durationMinutes = item.durationMinutes.toString(),
        version = item.version.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceRuleViews(items: MutableList<FreightInvoiceRule>): MutableList<FreightInvoiceRuleView> {
    val views = mutableListOf<FreightInvoiceRuleView>()
    for (item in items) {
        views.add(toFreightInvoiceRuleView(item))
    }
    return views
}

fun sumFreightInvoiceRuleView(view: FreightInvoiceRuleView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedQuantity.toDouble() + parsedOdometer.toDouble() + parsedMaximum
}
