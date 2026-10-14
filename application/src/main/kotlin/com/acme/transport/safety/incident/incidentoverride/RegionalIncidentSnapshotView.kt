package com.acme.transport.safety.incident.incidentoverride

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional incident snapshot. */
data class RegionalIncidentSnapshotView(
    var code: String,
    var name: String,
    var carrierText: String,
    var ownerId: String,
    var maximum: String,
    var source: String,
    var statusLabel: String,
)

fun toRegionalIncidentSnapshotView(item: RegionalIncidentSnapshot): RegionalIncidentSnapshotView {
    return RegionalIncidentSnapshotView(
        code = item.code,
        name = item.name,
        carrierText = truncate(item.carrier, 80),
        ownerId = truncate(item.ownerId, 40),
        maximum = "%.2f".format(item.maximum),
        source = truncate(item.source, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalIncidentSnapshotViews(items: MutableList<RegionalIncidentSnapshot>): MutableList<RegionalIncidentSnapshotView> {
    val views = mutableListOf<RegionalIncidentSnapshotView>()
    for (item in items) {
        views.add(toRegionalIncidentSnapshotView(item))
    }
    return views
}

fun sumRegionalIncidentSnapshotView(view: RegionalIncidentSnapshotView): Double {
    val parsedMaximum = view.maximum.toDouble()
    return parsedMaximum
}
