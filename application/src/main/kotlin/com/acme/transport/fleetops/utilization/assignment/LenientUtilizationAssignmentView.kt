package com.acme.transport.fleetops.utilization.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient utilization assignment. */
data class LenientUtilizationAssignmentView(
    var codeText: String,
    var label: String,
    var vehicleIdText: String,
    var unit: String,
    var trailerId: String,
    var revisionText: String,
    var pallets: String,
    var summary: String,
    var notesText: String,
    var minimum: String,
    var statusLabel: String,
)

fun toLenientUtilizationAssignmentView(item: LenientUtilizationAssignment): LenientUtilizationAssignmentView {
    return LenientUtilizationAssignmentView(
        codeText = truncate(item.code, 120),
        label = item.label,
        vehicleIdText = truncate(item.vehicleId, 120),
        unit = truncate(item.unit, 120),
        trailerId = item.trailerId,
        revisionText = item.revision.toString(),
        pallets = item.pallets.toString(),
        summary = item.summary,
        notesText = item.notes,
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientUtilizationAssignmentViews(items: MutableList<LenientUtilizationAssignment>): MutableList<LenientUtilizationAssignmentView> {
    val views = mutableListOf<LenientUtilizationAssignmentView>()
    for (item in items) {
        views.add(toLenientUtilizationAssignmentView(item))
    }
    return views
}

fun sumLenientUtilizationAssignmentView(view: LenientUtilizationAssignmentView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedRevision.toDouble() + parsedPallets.toDouble() + parsedMinimum
}
