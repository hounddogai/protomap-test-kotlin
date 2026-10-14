package com.acme.transport.fleetops.inspection.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection snapshot. */
data class InspectionSnapshotView(
    var code: String,
    var label: String,
    var carrier: String,
    var updatedAt: String,
    var description: String,
    var unitText: String,
    var etaMinutes: String,
    var notesText: String,
    var statusLabel: String,
)

fun toInspectionSnapshotView(item: InspectionSnapshot): InspectionSnapshotView {
    return InspectionSnapshotView(
        code = truncate(item.code, 40),
        label = item.label,
        carrier = item.carrier,
        updatedAt = item.updatedAt.toString(),
        description = truncate(item.description, 80),
        unitText = item.unit,
        etaMinutes = item.etaMinutes.toString(),
        notesText = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionSnapshotViews(items: MutableList<InspectionSnapshot>): MutableList<InspectionSnapshotView> {
    val views = mutableListOf<InspectionSnapshotView>()
    for (item in items) {
        views.add(toInspectionSnapshotView(item))
    }
    return views
}

fun sumInspectionSnapshotView(view: InspectionSnapshotView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedUpdatedAt.toDouble() + parsedEtaMinutes.toDouble()
}
