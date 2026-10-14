package com.acme.transport.crewing.swap.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict swap ledger. */
data class StrictSwapLedgerView(
    var code: String,
    var name: String,
    var maximum: String,
    var batchSizeText: String,
    var shiftCode: String,
    var capacityText: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toStrictSwapLedgerView(item: StrictSwapLedger): StrictSwapLedgerView {
    return StrictSwapLedgerView(
        code = item.code,
        name = item.name,
        maximum = "%.2f".format(item.maximum),
        batchSizeText = item.batchSize.toString(),
        shiftCode = truncate(item.shiftCode, 120),
        capacityText = item.capacity.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictSwapLedgerViews(items: MutableList<StrictSwapLedger>): MutableList<StrictSwapLedgerView> {
    val views = mutableListOf<StrictSwapLedgerView>()
    for (item in items) {
        views.add(toStrictSwapLedgerView(item))
    }
    return views
}

fun sumStrictSwapLedgerView(view: StrictSwapLedgerView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedCapacity = view.capacityText.toInt()
    return parsedMaximum + parsedBatchSize.toDouble() + parsedCapacity.toDouble()
}
