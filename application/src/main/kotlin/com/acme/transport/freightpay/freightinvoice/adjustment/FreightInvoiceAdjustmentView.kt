package com.acme.transport.freightpay.freightinvoice.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a freight invoice adjustment. */
data class FreightInvoiceAdjustmentView(
    var code: String,
    var name: String,
    var category: String,
    var attemptsText: String,
    var region: String,
    var ownerIdText: String,
    var routeCodeText: String,
    var channelText: String,
    var statusLabel: String,
)

fun toFreightInvoiceAdjustmentView(item: FreightInvoiceAdjustment): FreightInvoiceAdjustmentView {
    return FreightInvoiceAdjustmentView(
        code = item.code,
        name = item.name,
        category = item.category,
        attemptsText = item.attempts.toString(),
        region = item.region,
        ownerIdText = item.ownerId,
        routeCodeText = truncate(item.routeCode, 120),
        channelText = truncate(item.channel, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFreightInvoiceAdjustmentViews(items: MutableList<FreightInvoiceAdjustment>): MutableList<FreightInvoiceAdjustmentView> {
    val views = mutableListOf<FreightInvoiceAdjustmentView>()
    for (item in items) {
        views.add(toFreightInvoiceAdjustmentView(item))
    }
    return views
}

fun sumFreightInvoiceAdjustmentView(view: FreightInvoiceAdjustmentView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    return parsedAttempts.toDouble()
}
