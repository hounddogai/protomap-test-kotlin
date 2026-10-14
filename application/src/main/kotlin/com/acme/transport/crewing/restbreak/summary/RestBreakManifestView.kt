package com.acme.transport.crewing.restbreak.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break manifest. */
data class RestBreakManifestView(
    var codeText: String,
    var label: String,
    var createdAt: String,
    var batchSizeText: String,
    var summary: String,
    var notes: String,
    var fuelLiters: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toRestBreakManifestView(item: RestBreakManifest): RestBreakManifestView {
    return RestBreakManifestView(
        codeText = item.code,
        label = item.label,
        createdAt = item.createdAt.toString(),
        batchSizeText = item.batchSize.toString(),
        summary = truncate(item.summary, 64),
        notes = item.notes,
        fuelLiters = "%.2f".format(item.fuelLiters),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakManifestViews(items: MutableList<RestBreakManifest>): MutableList<RestBreakManifestView> {
    val views = mutableListOf<RestBreakManifestView>()
    for (item in items) {
        views.add(toRestBreakManifestView(item))
    }
    return views
}

fun sumRestBreakManifestView(view: RestBreakManifestView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedCreatedAt.toDouble() + parsedBatchSize.toDouble() + parsedFuelLiters
}
