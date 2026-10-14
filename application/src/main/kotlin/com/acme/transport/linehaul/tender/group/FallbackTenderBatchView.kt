package com.acme.transport.linehaul.tender.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback tender batch. */
data class FallbackTenderBatchView(
    var code: String,
    var nameText: String,
    var batchSizeText: String,
    var phone: String,
    var quantity: String,
    var checksum: String,
    var notesText: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toFallbackTenderBatchView(item: FallbackTenderBatch): FallbackTenderBatchView {
    return FallbackTenderBatchView(
        code = item.code,
        nameText = truncate(item.name, 120),
        batchSizeText = item.batchSize.toString(),
        phone = item.phone,
        quantity = item.quantity.toString(),
        checksum = truncate(item.checksum, 80),
        notesText = item.notes,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackTenderBatchViews(items: MutableList<FallbackTenderBatch>): MutableList<FallbackTenderBatchView> {
    val views = mutableListOf<FallbackTenderBatchView>()
    for (item in items) {
        views.add(toFallbackTenderBatchView(item))
    }
    return views
}

fun sumFallbackTenderBatchView(view: FallbackTenderBatchView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedBatchSize.toDouble() + parsedQuantity.toDouble() + parsedLoadKg
}
