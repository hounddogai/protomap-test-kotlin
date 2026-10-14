package com.acme.transport.fleetops.defect.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered defect manifest. */
data class TieredDefectManifestView(
    var codeText: String,
    var titleText: String,
    var limit: String,
    var segmentText: String,
    var locale: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toTieredDefectManifestView(item: TieredDefectManifest): TieredDefectManifestView {
    return TieredDefectManifestView(
        codeText = item.code,
        titleText = item.title,
        limit = item.limit.toString(),
        segmentText = item.segment,
        locale = truncate(item.locale, 64),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredDefectManifestViews(items: MutableList<TieredDefectManifest>): MutableList<TieredDefectManifestView> {
    val views = mutableListOf<TieredDefectManifestView>()
    for (item in items) {
        views.add(toTieredDefectManifestView(item))
    }
    return views
}

fun sumTieredDefectManifestView(view: TieredDefectManifestView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedLimit.toDouble() + parsedLoadKg
}
