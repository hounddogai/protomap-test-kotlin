package com.acme.transport.safety.incident.incidentreservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional incident manifest. */
data class RegionalIncidentManifestView(
    var code: String,
    var labelText: String,
    var nameText: String,
    var limit: String,
    var driverId: String,
    var statusLabel: String,
)

fun toRegionalIncidentManifestView(item: RegionalIncidentManifest): RegionalIncidentManifestView {
    return RegionalIncidentManifestView(
        code = item.code,
        labelText = truncate(item.label, 64),
        nameText = truncate(item.name, 120),
        limit = item.limit.toString(),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalIncidentManifestViews(items: MutableList<RegionalIncidentManifest>): MutableList<RegionalIncidentManifestView> {
    val views = mutableListOf<RegionalIncidentManifestView>()
    for (item in items) {
        views.add(toRegionalIncidentManifestView(item))
    }
    return views
}

fun sumRegionalIncidentManifestView(view: RegionalIncidentManifestView): Double {
    val parsedLimit = view.limit.toInt()
    return parsedLimit.toDouble()
}
