package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice tier. */
data class FreightInvoiceTierView(
    var code: String,
    var nameText: String,
    var visible: String,
    var notes: String,
    var reviewer: String,
    var pallets: String,
    var currency: String,
    var cost: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toFreightInvoiceTierView(item: FreightInvoiceTier): FreightInvoiceTierView {
    return FreightInvoiceTierView(
        code = truncate(item.code, 40),
        nameText = item.name,
        visible = "${item.visible}",
        notes = truncate(item.notes, 120),
        reviewer = item.reviewer,
        pallets = item.pallets.toString(),
        currency = truncate(item.currency, 80),
        cost = "%.2f".format(item.cost),
        descriptionText = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceTierViews(items: MutableList<FreightInvoiceTier>): MutableList<FreightInvoiceTierView> {
    val views = mutableListOf<FreightInvoiceTierView>()
    for (item in items) {
        views.add(toFreightInvoiceTierView(item))
    }
    return views
}

fun sumFreightInvoiceTierView(view: FreightInvoiceTierView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedPallets.toDouble() + parsedCost
}
