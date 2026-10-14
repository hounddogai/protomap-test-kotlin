package com.acme.transport.freightpay.freightinvoice.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending freight invoice group. */
data class PendingFreightInvoiceGroupView(
    var code: String,
    var title: String,
    var region: String,
    var label: String,
    var sourceText: String,
    var discountText: String,
    var distanceKm: String,
    var attemptsText: String,
    var threshold: String,
    var channel: String,
    var timezone: String,
    var statusLabel: String,
)

fun toPendingFreightInvoiceGroupView(item: PendingFreightInvoiceGroup): PendingFreightInvoiceGroupView {
    return PendingFreightInvoiceGroupView(
        code = truncate(item.code, 64),
        title = item.title,
        region = item.region,
        label = item.label,
        sourceText = item.source,
        discountText = "%.2f".format(item.discount),
        distanceKm = "%.2f".format(item.distanceKm),
        attemptsText = item.attempts.toString(),
        threshold = "%.2f".format(item.threshold),
        channel = item.channel,
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingFreightInvoiceGroupViews(items: MutableList<PendingFreightInvoiceGroup>): MutableList<PendingFreightInvoiceGroupView> {
    val views = mutableListOf<PendingFreightInvoiceGroupView>()
    for (item in items) {
        views.add(toPendingFreightInvoiceGroupView(item))
    }
    return views
}

fun sumPendingFreightInvoiceGroupView(view: PendingFreightInvoiceGroupView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedDiscount + parsedDistanceKm + parsedAttempts.toDouble()
}
