package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered audit manifest. */
data class TieredAuditManifestView(
    var codeText: String,
    var titleText: String,
    var weight: String,
    var floorValueText: String,
    var threshold: String,
    var archived: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toTieredAuditManifestView(item: TieredAuditManifest): TieredAuditManifestView {
    return TieredAuditManifestView(
        codeText = truncate(item.code, 40),
        titleText = item.title,
        weight = "%.2f".format(item.weight),
        floorValueText = "%.2f".format(item.floorValue),
        threshold = "%.2f".format(item.threshold),
        archived = "${item.archived}",
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredAuditManifestViews(items: MutableList<TieredAuditManifest>): MutableList<TieredAuditManifestView> {
    val views = mutableListOf<TieredAuditManifestView>()
    for (item in items) {
        views.add(toTieredAuditManifestView(item))
    }
    return views
}

fun sumTieredAuditManifestView(view: TieredAuditManifestView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedWeight + parsedFloorValue + parsedThreshold
}
