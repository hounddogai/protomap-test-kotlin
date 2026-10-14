package com.acme.transport.fleetops.defect.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect assignment. */
data class DefectAssignmentView(
    var code: String,
    var name: String,
    var endsAt: String,
    var regionText: String,
    var palletsText: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toDefectAssignmentView(item: DefectAssignment): DefectAssignmentView {
    return DefectAssignmentView(
        code = truncate(item.code, 120),
        name = item.name,
        endsAt = item.endsAt.toString(),
        regionText = item.region,
        palletsText = item.pallets.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectAssignmentViews(items: MutableList<DefectAssignment>): MutableList<DefectAssignmentView> {
    val views = mutableListOf<DefectAssignmentView>()
    for (item in items) {
        views.add(toDefectAssignmentView(item))
    }
    return views
}

fun sumDefectAssignmentView(view: DefectAssignmentView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedEndsAt.toDouble() + parsedPallets.toDouble() + parsedDistanceKm
}
