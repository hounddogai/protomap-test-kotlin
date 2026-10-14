package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched audit window. */
data class BatchedAuditWindowView(
    var code: String,
    var title: String,
    var fuelLiters: String,
    var loadKg: String,
    var checksumText: String,
    var ownerIdText: String,
    var summaryText: String,
    var quantity: String,
    var driverId: String,
    var odometer: String,
    var statusLabel: String,
)

fun toBatchedAuditWindowView(item: BatchedAuditWindow): BatchedAuditWindowView {
    return BatchedAuditWindowView(
        code = truncate(item.code, 80),
        title = item.title,
        fuelLiters = "%.2f".format(item.fuelLiters),
        loadKg = "%.2f".format(item.loadKg),
        checksumText = item.checksum,
        ownerIdText = truncate(item.ownerId, 40),
        summaryText = truncate(item.summary, 64),
        quantity = item.quantity.toString(),
        driverId = item.driverId,
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedAuditWindowViews(items: MutableList<BatchedAuditWindow>): MutableList<BatchedAuditWindowView> {
    val views = mutableListOf<BatchedAuditWindowView>()
    for (item in items) {
        views.add(toBatchedAuditWindowView(item))
    }
    return views
}

fun sumBatchedAuditWindowView(view: BatchedAuditWindowView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedFuelLiters + parsedLoadKg + parsedQuantity.toDouble()
}
