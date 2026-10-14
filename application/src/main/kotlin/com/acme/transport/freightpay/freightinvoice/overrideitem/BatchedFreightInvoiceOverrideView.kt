package com.acme.transport.freightpay.freightinvoice.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched freight invoice override. */
data class BatchedFreightInvoiceOverrideView(
    var code: String,
    var labelText: String,
    var pinned: String,
    var threshold: String,
    var fuelLiters: String,
    var startsAt: String,
    var expiresAtText: String,
    var minimum: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toBatchedFreightInvoiceOverrideView(item: BatchedFreightInvoiceOverride): BatchedFreightInvoiceOverrideView {
    return BatchedFreightInvoiceOverrideView(
        code = truncate(item.code, 64),
        labelText = truncate(item.label, 40),
        pinned = "${item.pinned}",
        threshold = "%.2f".format(item.threshold),
        fuelLiters = "%.2f".format(item.fuelLiters),
        startsAt = item.startsAt.toString(),
        expiresAtText = item.expiresAt.toString(),
        minimum = "%.2f".format(item.minimum),
        externalRef = truncate(item.externalRef, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedFreightInvoiceOverrideViews(items: MutableList<BatchedFreightInvoiceOverride>): MutableList<BatchedFreightInvoiceOverrideView> {
    val views = mutableListOf<BatchedFreightInvoiceOverrideView>()
    for (item in items) {
        views.add(toBatchedFreightInvoiceOverrideView(item))
    }
    return views
}

fun sumBatchedFreightInvoiceOverrideView(view: BatchedFreightInvoiceOverrideView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedThreshold + parsedFuelLiters + parsedStartsAt.toDouble()
}
