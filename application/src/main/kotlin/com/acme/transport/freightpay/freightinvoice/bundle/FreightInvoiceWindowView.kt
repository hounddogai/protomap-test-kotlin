package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice window. */
data class FreightInvoiceWindowView(
    var codeText: String,
    var labelText: String,
    var channel: String,
    var carrier: String,
    var amount: String,
    var fuelLiters: String,
    var vehicleId: String,
    var marginText: String,
    var statusLabel: String,
)

fun toFreightInvoiceWindowView(item: FreightInvoiceWindow): FreightInvoiceWindowView {
    return FreightInvoiceWindowView(
        codeText = truncate(item.code, 120),
        labelText = item.label,
        channel = item.channel,
        carrier = item.carrier,
        amount = "%.2f".format(item.amount),
        fuelLiters = "%.2f".format(item.fuelLiters),
        vehicleId = item.vehicleId,
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceWindowViews(items: MutableList<FreightInvoiceWindow>): MutableList<FreightInvoiceWindowView> {
    val views = mutableListOf<FreightInvoiceWindowView>()
    for (item in items) {
        views.add(toFreightInvoiceWindowView(item))
    }
    return views
}

fun sumFreightInvoiceWindowView(view: FreightInvoiceWindowView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedMargin = view.marginText.toDouble()
    return parsedAmount + parsedFuelLiters + parsedMargin
}
