package com.acme.transport.customs.inspection.inspectiontarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection manifest. */
data class InspectionManifestView(
    var code: String,
    var name: String,
    var versionText: String,
    var floorValueText: String,
    var maximumText: String,
    var dock: String,
    var segment: String,
    var revision: String,
    var statusLabel: String,
)

fun toInspectionManifestView(item: InspectionManifest): InspectionManifestView {
    return InspectionManifestView(
        code = item.code,
        name = truncate(item.name, 64),
        versionText = item.version.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        maximumText = "%.2f".format(item.maximum),
        dock = item.dock,
        segment = item.segment,
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionManifestViews(items: MutableList<InspectionManifest>): MutableList<InspectionManifestView> {
    val views = mutableListOf<InspectionManifestView>()
    for (item in items) {
        views.add(toInspectionManifestView(item))
    }
    return views
}

fun sumInspectionManifestView(view: InspectionManifestView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedVersion.toDouble() + parsedFloorValue + parsedMaximum
}
