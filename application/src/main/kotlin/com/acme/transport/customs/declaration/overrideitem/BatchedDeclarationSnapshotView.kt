package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched declaration snapshot. */
data class BatchedDeclarationSnapshotView(
    var codeText: String,
    var label: String,
    var batchSize: String,
    var maximum: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toBatchedDeclarationSnapshotView(item: BatchedDeclarationSnapshot): BatchedDeclarationSnapshotView {
    return BatchedDeclarationSnapshotView(
        codeText = item.code,
        label = truncate(item.label, 64),
        batchSize = item.batchSize.toString(),
        maximum = "%.2f".format(item.maximum),
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDeclarationSnapshotViews(items: MutableList<BatchedDeclarationSnapshot>): MutableList<BatchedDeclarationSnapshotView> {
    val views = mutableListOf<BatchedDeclarationSnapshotView>()
    for (item in items) {
        views.add(toBatchedDeclarationSnapshotView(item))
    }
    return views
}

fun sumBatchedDeclarationSnapshotView(view: BatchedDeclarationSnapshotView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedBatchSize.toDouble() + parsedMaximum + parsedEndsAt.toDouble()
}
