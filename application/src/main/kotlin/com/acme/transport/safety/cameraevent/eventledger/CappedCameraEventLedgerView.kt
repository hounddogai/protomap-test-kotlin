package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped camera event ledger. */
data class CappedCameraEventLedgerView(
    var code: String,
    var nameText: String,
    var batchSize: String,
    var ratioText: String,
    var notes: String,
    var vehicleId: String,
    var expiresAt: String,
    var minimum: String,
    var attempts: String,
    var threshold: String,
    var statusLabel: String,
)

fun toCappedCameraEventLedgerView(item: CappedCameraEventLedger): CappedCameraEventLedgerView {
    return CappedCameraEventLedgerView(
        code = item.code,
        nameText = item.name,
        batchSize = item.batchSize.toString(),
        ratioText = "%.2f".format(item.ratio),
        notes = item.notes,
        vehicleId = truncate(item.vehicleId, 120),
        expiresAt = item.expiresAt.toString(),
        minimum = "%.2f".format(item.minimum),
        attempts = item.attempts.toString(),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedCameraEventLedgerViews(items: MutableList<CappedCameraEventLedger>): MutableList<CappedCameraEventLedgerView> {
    val views = mutableListOf<CappedCameraEventLedgerView>()
    for (item in items) {
        views.add(toCappedCameraEventLedgerView(item))
    }
    return views
}

fun sumCappedCameraEventLedgerView(view: CappedCameraEventLedgerView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedRatio = view.ratioText.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedBatchSize.toDouble() + parsedRatio + parsedExpiresAt.toDouble()
}
