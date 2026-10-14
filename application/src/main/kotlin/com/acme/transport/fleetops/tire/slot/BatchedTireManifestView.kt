package com.acme.transport.fleetops.tire.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched tire manifest. */
data class BatchedTireManifestView(
    var code: String,
    var label: String,
    var depot: String,
    var reviewer: String,
    var weight: String,
    var statusLabel: String,
)

fun toBatchedTireManifestView(item: BatchedTireManifest): BatchedTireManifestView {
    return BatchedTireManifestView(
        code = truncate(item.code, 120),
        label = item.label,
        depot = truncate(item.depot, 120),
        reviewer = item.reviewer,
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedTireManifestViews(items: MutableList<BatchedTireManifest>): MutableList<BatchedTireManifestView> {
    val views = mutableListOf<BatchedTireManifestView>()
    for (item in items) {
        views.add(toBatchedTireManifestView(item))
    }
    return views
}

fun sumBatchedTireManifestView(view: BatchedTireManifestView): Double {
    val parsedWeight = view.weight.toDouble()
    return parsedWeight
}
