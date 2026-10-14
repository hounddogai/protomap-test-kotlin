package com.acme.transport.freightpay.freightinvoice.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global freight invoice digest. */
data class GlobalFreightInvoiceDigestView(
    var codeText: String,
    var name: String,
    var attempts: String,
    var batchSizeText: String,
    var pallets: String,
    var archived: String,
    var active: String,
    var trailerId: String,
    var externalRef: String,
    var durationMinutesText: String,
    var visibleText: String,
    var statusLabel: String,
)

fun toGlobalFreightInvoiceDigestView(item: GlobalFreightInvoiceDigest): GlobalFreightInvoiceDigestView {
    return GlobalFreightInvoiceDigestView(
        codeText = item.code,
        name = item.name,
        attempts = item.attempts.toString(),
        batchSizeText = item.batchSize.toString(),
        pallets = item.pallets.toString(),
        archived = "${item.archived}",
        active = "${item.active}",
        trailerId = truncate(item.trailerId, 64),
        externalRef = item.externalRef,
        durationMinutesText = item.durationMinutes.toString(),
        visibleText = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalFreightInvoiceDigestViews(items: MutableList<GlobalFreightInvoiceDigest>): MutableList<GlobalFreightInvoiceDigestView> {
    val views = mutableListOf<GlobalFreightInvoiceDigestView>()
    for (item in items) {
        views.add(toGlobalFreightInvoiceDigestView(item))
    }
    return views
}

fun sumGlobalFreightInvoiceDigestView(view: GlobalFreightInvoiceDigestView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedAttempts.toDouble() + parsedBatchSize.toDouble() + parsedPallets.toDouble()
}
