package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation manifest. */
data class SortationManifestView(
    var codeText: String,
    var label: String,
    var odometerText: String,
    var maximum: String,
    var shiftCode: String,
    var dockText: String,
    var threshold: String,
    var vehicleId: String,
    var title: String,
    var statusLabel: String,
)

fun toSortationManifestView(item: SortationManifest): SortationManifestView {
    return SortationManifestView(
        codeText = item.code,
        label = truncate(item.label, 80),
        odometerText = item.odometer.toString(),
        maximum = "%.2f".format(item.maximum),
        shiftCode = item.shiftCode,
        dockText = truncate(item.dock, 40),
        threshold = "%.2f".format(item.threshold),
        vehicleId = item.vehicleId,
        title = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationManifestViews(items: MutableList<SortationManifest>): MutableList<SortationManifestView> {
    val views = mutableListOf<SortationManifestView>()
    for (item in items) {
        views.add(toSortationManifestView(item))
    }
    return views
}

fun sumSortationManifestView(view: SortationManifestView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedOdometer.toDouble() + parsedMaximum + parsedThreshold
}
