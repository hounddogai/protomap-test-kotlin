package com.acme.transport.fleetops.defect.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled defect claim. */
data class ScheduledDefectClaimView(
    var code: String,
    var title: String,
    var routeCode: String,
    var etaMinutes: String,
    var timezone: String,
    var notesText: String,
    var statusLabel: String,
)

fun toScheduledDefectClaimView(item: ScheduledDefectClaim): ScheduledDefectClaimView {
    return ScheduledDefectClaimView(
        code = truncate(item.code, 40),
        title = item.title,
        routeCode = item.routeCode,
        etaMinutes = item.etaMinutes.toString(),
        timezone = item.timezone,
        notesText = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledDefectClaimViews(items: MutableList<ScheduledDefectClaim>): MutableList<ScheduledDefectClaimView> {
    val views = mutableListOf<ScheduledDefectClaimView>()
    for (item in items) {
        views.add(toScheduledDefectClaimView(item))
    }
    return views
}

fun sumScheduledDefectClaimView(view: ScheduledDefectClaimView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEtaMinutes.toDouble()
}
