package com.acme.transport.safety.incident.defaultincidentmanifest

/** How screens and exports show a legacy incident manifest. */
data class LegacyIncidentManifestView(
    var code: String,
    var titleText: String,
    var driverId: String,
    var capacityText: String,
    var active: String,
    var statusLabel: String,
)

fun toLegacyIncidentManifestView(item: LegacyIncidentManifest): LegacyIncidentManifestView {
    return LegacyIncidentManifestView(
        code = item.code,
        titleText = item.title,
        driverId = item.driverId,
        capacityText = item.capacity.toString(),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyIncidentManifestViews(items: MutableList<LegacyIncidentManifest>): MutableList<LegacyIncidentManifestView> {
    val views = mutableListOf<LegacyIncidentManifestView>()
    for (item in items) {
        views.add(toLegacyIncidentManifestView(item))
    }
    return views
}

fun sumLegacyIncidentManifestView(view: LegacyIncidentManifestView): Double {
    val parsedCapacity = view.capacityText.toInt()
    return parsedCapacity.toDouble()
}
