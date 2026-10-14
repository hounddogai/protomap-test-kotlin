package com.acme.transport.fleetops.tire.tirebudget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive tire manifest. */
data class AdaptiveTireManifestView(
    var code: String,
    var title: String,
    var description: String,
    var driverId: String,
    var region: String,
    var priorityText: String,
    var category: String,
    var statusLabel: String,
)

fun toAdaptiveTireManifestView(item: AdaptiveTireManifest): AdaptiveTireManifestView {
    return AdaptiveTireManifestView(
        code = item.code,
        title = item.title,
        description = item.description,
        driverId = truncate(item.driverId, 120),
        region = item.region,
        priorityText = item.priority.toString(),
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveTireManifestViews(items: MutableList<AdaptiveTireManifest>): MutableList<AdaptiveTireManifestView> {
    val views = mutableListOf<AdaptiveTireManifestView>()
    for (item in items) {
        views.add(toAdaptiveTireManifestView(item))
    }
    return views
}

fun sumAdaptiveTireManifestView(view: AdaptiveTireManifestView): Double {
    val parsedPriority = view.priorityText.toInt()
    return parsedPriority.toDouble()
}
