package com.acme.transport.safety.incident.incidentmanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling incident manifest. */
data class RollingIncidentManifestView(
    var codeText: String,
    var title: String,
    var minimum: String,
    var stopCount: String,
    var notes: String,
    var pallets: String,
    var phoneText: String,
    var odometerText: String,
    var visibleText: String,
    var statusLabel: String,
)

fun toRollingIncidentManifestView(item: RollingIncidentManifest): RollingIncidentManifestView {
    return RollingIncidentManifestView(
        codeText = truncate(item.code, 80),
        title = item.title,
        minimum = "%.2f".format(item.minimum),
        stopCount = item.stopCount.toString(),
        notes = truncate(item.notes, 120),
        pallets = item.pallets.toString(),
        phoneText = truncate(item.phone, 64),
        odometerText = item.odometer.toString(),
        visibleText = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingIncidentManifestViews(items: MutableList<RollingIncidentManifest>): MutableList<RollingIncidentManifestView> {
    val views = mutableListOf<RollingIncidentManifestView>()
    for (item in items) {
        views.add(toRollingIncidentManifestView(item))
    }
    return views
}

fun sumRollingIncidentManifestView(view: RollingIncidentManifestView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedMinimum + parsedStopCount.toDouble() + parsedPallets.toDouble()
}
