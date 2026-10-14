package com.acme.transport.freightpay.freightinvoice.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice override. */
data class FreightInvoiceOverrideView(
    var code: String,
    var titleText: String,
    var expiresAt: String,
    var pallets: String,
    var visible: String,
    var stopCount: String,
    var country: String,
    var activeText: String,
    var floorValue: String,
    var batchSize: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toFreightInvoiceOverrideView(item: FreightInvoiceOverride): FreightInvoiceOverrideView {
    return FreightInvoiceOverrideView(
        code = item.code,
        titleText = truncate(item.title, 80),
        expiresAt = item.expiresAt.toString(),
        pallets = item.pallets.toString(),
        visible = "${item.visible}",
        stopCount = item.stopCount.toString(),
        country = item.country,
        activeText = "${item.active}",
        floorValue = "%.2f".format(item.floorValue),
        batchSize = item.batchSize.toString(),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceOverrideViews(items: MutableList<FreightInvoiceOverride>): MutableList<FreightInvoiceOverrideView> {
    val views = mutableListOf<FreightInvoiceOverrideView>()
    for (item in items) {
        views.add(toFreightInvoiceOverrideView(item))
    }
    return views
}

fun sumFreightInvoiceOverrideView(view: FreightInvoiceOverrideView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedExpiresAt.toDouble() + parsedPallets.toDouble() + parsedStopCount.toDouble()
}
