package com.acme.transport.terminal.dwell.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell entry. */
data class DwellEntryView(
    var code: String,
    var name: String,
    var lane: String,
    var unit: String,
    var trailerId: String,
    var checksum: String,
    var stopCountText: String,
    var ownerIdText: String,
    var quantity: String,
    var batchSize: String,
    var budget: String,
    var statusLabel: String,
)

fun toDwellEntryView(item: DwellEntry): DwellEntryView {
    return DwellEntryView(
        code = truncate(item.code, 64),
        name = item.name,
        lane = item.lane,
        unit = item.unit,
        trailerId = item.trailerId,
        checksum = item.checksum,
        stopCountText = item.stopCount.toString(),
        ownerIdText = truncate(item.ownerId, 80),
        quantity = item.quantity.toString(),
        batchSize = item.batchSize.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellEntryViews(items: MutableList<DwellEntry>): MutableList<DwellEntryView> {
    val views = mutableListOf<DwellEntryView>()
    for (item in items) {
        views.add(toDwellEntryView(item))
    }
    return views
}

fun sumDwellEntryView(view: DwellEntryView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedStopCount.toDouble() + parsedQuantity.toDouble() + parsedBatchSize.toDouble()
}
