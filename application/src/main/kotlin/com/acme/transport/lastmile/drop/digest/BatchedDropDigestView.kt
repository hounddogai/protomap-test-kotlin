package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched drop digest. */
data class BatchedDropDigestView(
    var code: String,
    var title: String,
    var ownerId: String,
    var sequence: String,
    var externalRefText: String,
    var countryText: String,
    var marginText: String,
    var checksum: String,
    var dock: String,
    var pallets: String,
    var statusLabel: String,
)

fun toBatchedDropDigestView(item: BatchedDropDigest): BatchedDropDigestView {
    return BatchedDropDigestView(
        code = item.code,
        title = truncate(item.title, 64),
        ownerId = item.ownerId,
        sequence = item.sequence.toString(),
        externalRefText = item.externalRef,
        countryText = item.country,
        marginText = "%.2f".format(item.margin),
        checksum = item.checksum,
        dock = truncate(item.dock, 120),
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDropDigestViews(items: MutableList<BatchedDropDigest>): MutableList<BatchedDropDigestView> {
    val views = mutableListOf<BatchedDropDigestView>()
    for (item in items) {
        views.add(toBatchedDropDigestView(item))
    }
    return views
}

fun sumBatchedDropDigestView(view: BatchedDropDigestView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedMargin = view.marginText.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedSequence.toDouble() + parsedMargin + parsedPallets.toDouble()
}
