package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive inspection manifest. */
data class AdaptiveInspectionManifestView(
    var code: String,
    var title: String,
    var ownerId: String,
    var source: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toAdaptiveInspectionManifestView(item: AdaptiveInspectionManifest): AdaptiveInspectionManifestView {
    return AdaptiveInspectionManifestView(
        code = item.code,
        title = truncate(item.title, 40),
        ownerId = truncate(item.ownerId, 40),
        source = item.source,
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveInspectionManifestViews(items: MutableList<AdaptiveInspectionManifest>): MutableList<AdaptiveInspectionManifestView> {
    val views = mutableListOf<AdaptiveInspectionManifestView>()
    for (item in items) {
        views.add(toAdaptiveInspectionManifestView(item))
    }
    return views
}

fun sumAdaptiveInspectionManifestView(view: AdaptiveInspectionManifestView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
