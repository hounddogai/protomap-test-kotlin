package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched defect digest. */
data class BatchedDefectDigestView(
    var code: String,
    var label: String,
    var sourceText: String,
    var regionText: String,
    var checksum: String,
    var quantity: String,
    var statusLabel: String,
)

fun toBatchedDefectDigestView(item: BatchedDefectDigest): BatchedDefectDigestView {
    return BatchedDefectDigestView(
        code = item.code,
        label = item.label,
        sourceText = item.source,
        regionText = item.region,
        checksum = truncate(item.checksum, 40),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDefectDigestViews(items: MutableList<BatchedDefectDigest>): MutableList<BatchedDefectDigestView> {
    val views = mutableListOf<BatchedDefectDigestView>()
    for (item in items) {
        views.add(toBatchedDefectDigestView(item))
    }
    return views
}

fun sumBatchedDefectDigestView(view: BatchedDefectDigestView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
