package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict defect milestone. */
data class StrictDefectMilestoneView(
    var code: String,
    var labelText: String,
    var updatedAt: String,
    var notes: String,
    var depotText: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toStrictDefectMilestoneView(item: StrictDefectMilestone): StrictDefectMilestoneView {
    return StrictDefectMilestoneView(
        code = truncate(item.code, 64),
        labelText = item.label,
        updatedAt = item.updatedAt.toString(),
        notes = item.notes,
        depotText = truncate(item.depot, 80),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDefectMilestoneViews(items: MutableList<StrictDefectMilestone>): MutableList<StrictDefectMilestoneView> {
    val views = mutableListOf<StrictDefectMilestoneView>()
    for (item in items) {
        views.add(toStrictDefectMilestoneView(item))
    }
    return views
}

fun sumStrictDefectMilestoneView(view: StrictDefectMilestoneView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedUpdatedAt.toDouble() + parsedExpiresAt.toDouble()
}
