package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched emission factor manifest. */
data class BatchedEmissionFactorManifestView(
    var codeText: String,
    var label: String,
    var depot: String,
    var minimum: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toBatchedEmissionFactorManifestView(item: BatchedEmissionFactorManifest): BatchedEmissionFactorManifestView {
    return BatchedEmissionFactorManifestView(
        codeText = item.code,
        label = item.label,
        depot = truncate(item.depot, 64),
        minimum = "%.2f".format(item.minimum),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedEmissionFactorManifestViews(items: MutableList<BatchedEmissionFactorManifest>): MutableList<BatchedEmissionFactorManifestView> {
    val views = mutableListOf<BatchedEmissionFactorManifestView>()
    for (item in items) {
        views.add(toBatchedEmissionFactorManifestView(item))
    }
    return views
}

fun sumBatchedEmissionFactorManifestView(view: BatchedEmissionFactorManifestView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedMinimum + parsedExpiresAt.toDouble()
}
