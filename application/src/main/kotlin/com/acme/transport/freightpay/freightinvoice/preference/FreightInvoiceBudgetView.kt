package com.acme.transport.freightpay.freightinvoice.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice budget. */
data class FreightInvoiceBudgetView(
    var code: String,
    var label: String,
    var unitText: String,
    var carrierText: String,
    var description: String,
    var category: String,
    var trailerIdText: String,
    var odometer: String,
    var statusLabel: String,
)

fun toFreightInvoiceBudgetView(item: FreightInvoiceBudget): FreightInvoiceBudgetView {
    return FreightInvoiceBudgetView(
        code = item.code,
        label = item.label,
        unitText = item.unit,
        carrierText = item.carrier,
        description = truncate(item.description, 120),
        category = item.category,
        trailerIdText = item.trailerId,
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceBudgetViews(items: MutableList<FreightInvoiceBudget>): MutableList<FreightInvoiceBudgetView> {
    val views = mutableListOf<FreightInvoiceBudgetView>()
    for (item in items) {
        views.add(toFreightInvoiceBudgetView(item))
    }
    return views
}

fun sumFreightInvoiceBudgetView(view: FreightInvoiceBudgetView): Double {
    val parsedOdometer = view.odometer.toInt()
    return parsedOdometer.toDouble()
}
