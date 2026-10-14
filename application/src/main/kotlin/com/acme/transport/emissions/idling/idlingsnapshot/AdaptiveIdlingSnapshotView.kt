package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive idling snapshot. */
data class AdaptiveIdlingSnapshotView(
    var code: String,
    var title: String,
    var expiresAt: String,
    var limit: String,
    var batchSizeText: String,
    var rate: String,
    var activeText: String,
    var currency: String,
    var weight: String,
    var statusLabel: String,
)

fun toAdaptiveIdlingSnapshotView(item: AdaptiveIdlingSnapshot): AdaptiveIdlingSnapshotView {
    return AdaptiveIdlingSnapshotView(
        code = item.code,
        title = truncate(item.title, 120),
        expiresAt = item.expiresAt.toString(),
        limit = item.limit.toString(),
        batchSizeText = item.batchSize.toString(),
        rate = "%.2f".format(item.rate),
        activeText = "${item.active}",
        currency = item.currency,
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveIdlingSnapshotViews(items: MutableList<AdaptiveIdlingSnapshot>): MutableList<AdaptiveIdlingSnapshotView> {
    val views = mutableListOf<AdaptiveIdlingSnapshotView>()
    for (item in items) {
        views.add(toAdaptiveIdlingSnapshotView(item))
    }
    return views
}

fun sumAdaptiveIdlingSnapshotView(view: AdaptiveIdlingSnapshotView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedExpiresAt.toDouble() + parsedLimit.toDouble() + parsedBatchSize.toDouble()
}
