package com.acme.transport.fleetops.inspection.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection audit. */
data class InspectionAuditView(
    var code: String,
    var title: String,
    var carrier: String,
    var score: String,
    var ownerIdText: String,
    var timezone: String,
    var durationMinutesText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toInspectionAuditView(item: InspectionAudit): InspectionAuditView {
    return InspectionAuditView(
        code = item.code,
        title = item.title,
        carrier = item.carrier,
        score = "%.2f".format(item.score),
        ownerIdText = item.ownerId,
        timezone = truncate(item.timezone, 120),
        durationMinutesText = item.durationMinutes.toString(),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionAuditViews(items: MutableList<InspectionAudit>): MutableList<InspectionAuditView> {
    val views = mutableListOf<InspectionAuditView>()
    for (item in items) {
        views.add(toInspectionAuditView(item))
    }
    return views
}

fun sumInspectionAuditView(view: InspectionAuditView): Double {
    val parsedScore = view.score.toDouble()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedScore + parsedDurationMinutes.toDouble()
}
