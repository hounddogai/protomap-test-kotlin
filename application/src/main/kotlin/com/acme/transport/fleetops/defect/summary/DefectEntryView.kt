package com.acme.transport.fleetops.defect.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect entry. */
data class DefectEntryView(
    var code: String,
    var titleText: String,
    var unit: String,
    var category: String,
    var notesText: String,
    var regionText: String,
    var distanceKm: String,
    var expiresAt: String,
    var reviewer: String,
    var depotText: String,
    var active: String,
    var statusLabel: String,
)

fun toDefectEntryView(item: DefectEntry): DefectEntryView {
    return DefectEntryView(
        code = item.code,
        titleText = truncate(item.title, 64),
        unit = item.unit,
        category = item.category,
        notesText = item.notes,
        regionText = item.region,
        distanceKm = "%.2f".format(item.distanceKm),
        expiresAt = item.expiresAt.toString(),
        reviewer = item.reviewer,
        depotText = item.depot,
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectEntryViews(items: MutableList<DefectEntry>): MutableList<DefectEntryView> {
    val views = mutableListOf<DefectEntryView>()
    for (item in items) {
        views.add(toDefectEntryView(item))
    }
    return views
}

fun sumDefectEntryView(view: DefectEntryView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedDistanceKm + parsedExpiresAt.toDouble()
}
