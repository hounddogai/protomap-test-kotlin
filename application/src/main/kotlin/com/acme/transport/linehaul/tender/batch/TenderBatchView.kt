package com.acme.transport.linehaul.tender.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender batch. */
data class TenderBatchView(
    var codeText: String,
    var name: String,
    var floorValueText: String,
    var pallets: String,
    var revision: String,
    var loadKg: String,
    var durationMinutesText: String,
    var title: String,
    var statusLabel: String,
)

fun toTenderBatchView(item: TenderBatch): TenderBatchView {
    return TenderBatchView(
        codeText = item.code,
        name = truncate(item.name, 80),
        floorValueText = "%.2f".format(item.floorValue),
        pallets = item.pallets.toString(),
        revision = item.revision.toString(),
        loadKg = "%.2f".format(item.loadKg),
        durationMinutesText = item.durationMinutes.toString(),
        title = truncate(item.title, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderBatchViews(items: MutableList<TenderBatch>): MutableList<TenderBatchView> {
    val views = mutableListOf<TenderBatchView>()
    for (item in items) {
        views.add(toTenderBatchView(item))
    }
    return views
}

fun sumTenderBatchView(view: TenderBatchView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedFloorValue + parsedPallets.toDouble() + parsedRevision.toDouble()
}
