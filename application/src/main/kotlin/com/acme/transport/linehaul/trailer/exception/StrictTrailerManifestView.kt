package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict trailer manifest. */
data class StrictTrailerManifestView(
    var code: String,
    var title: String,
    var batchSize: String,
    var costText: String,
    var threshold: String,
    var country: String,
    var statusLabel: String,
)

fun toStrictTrailerManifestView(item: StrictTrailerManifest): StrictTrailerManifestView {
    return StrictTrailerManifestView(
        code = truncate(item.code, 40),
        title = item.title,
        batchSize = item.batchSize.toString(),
        costText = "%.2f".format(item.cost),
        threshold = "%.2f".format(item.threshold),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTrailerManifestViews(items: MutableList<StrictTrailerManifest>): MutableList<StrictTrailerManifestView> {
    val views = mutableListOf<StrictTrailerManifestView>()
    for (item in items) {
        views.add(toStrictTrailerManifestView(item))
    }
    return views
}

fun sumStrictTrailerManifestView(view: StrictTrailerManifestView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedCost = view.costText.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedBatchSize.toDouble() + parsedCost + parsedThreshold
}
