package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive route offset manifest. */
data class AdaptiveRouteOffsetManifestView(
    var codeText: String,
    var label: String,
    var unitText: String,
    var source: String,
    var trailerId: String,
    var quantityText: String,
    var titleText: String,
    var statusLabel: String,
)

fun toAdaptiveRouteOffsetManifestView(item: AdaptiveRouteOffsetManifest): AdaptiveRouteOffsetManifestView {
    return AdaptiveRouteOffsetManifestView(
        codeText = item.code,
        label = item.label,
        unitText = item.unit,
        source = truncate(item.source, 40),
        trailerId = item.trailerId,
        quantityText = item.quantity.toString(),
        titleText = truncate(item.title, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveRouteOffsetManifestViews(items: MutableList<AdaptiveRouteOffsetManifest>): MutableList<AdaptiveRouteOffsetManifestView> {
    val views = mutableListOf<AdaptiveRouteOffsetManifestView>()
    for (item in items) {
        views.add(toAdaptiveRouteOffsetManifestView(item))
    }
    return views
}

fun sumAdaptiveRouteOffsetManifestView(view: AdaptiveRouteOffsetManifestView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
