package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice bundle. */
data class FreightInvoiceBundleView(
    var codeText: String,
    var name: String,
    var trailerId: String,
    var startsAt: String,
    var depot: String,
    var shiftCode: String,
    var source: String,
    var driverId: String,
    var quantity: String,
    var statusLabel: String,
)

fun toFreightInvoiceBundleView(item: FreightInvoiceBundle): FreightInvoiceBundleView {
    return FreightInvoiceBundleView(
        codeText = truncate(item.code, 80),
        name = truncate(item.name, 120),
        trailerId = item.trailerId,
        startsAt = item.startsAt.toString(),
        depot = truncate(item.depot, 120),
        shiftCode = item.shiftCode,
        source = item.source,
        driverId = truncate(item.driverId, 120),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceBundleViews(items: MutableList<FreightInvoiceBundle>): MutableList<FreightInvoiceBundleView> {
    val views = mutableListOf<FreightInvoiceBundleView>()
    for (item in items) {
        views.add(toFreightInvoiceBundleView(item))
    }
    return views
}

fun sumFreightInvoiceBundleView(view: FreightInvoiceBundleView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedStartsAt.toDouble() + parsedQuantity.toDouble()
}
