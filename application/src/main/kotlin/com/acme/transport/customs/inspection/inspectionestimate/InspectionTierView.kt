package com.acme.transport.customs.inspection.inspectionestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection tier. */
data class InspectionTierView(
    var code: String,
    var label: String,
    var routeCode: String,
    var durationMinutes: String,
    var archived: String,
    var reviewer: String,
    var notes: String,
    var statusLabel: String,
)

fun toInspectionTierView(item: InspectionTier): InspectionTierView {
    return InspectionTierView(
        code = item.code,
        label = truncate(item.label, 64),
        routeCode = truncate(item.routeCode, 64),
        durationMinutes = item.durationMinutes.toString(),
        archived = "${item.archived}",
        reviewer = item.reviewer,
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionTierViews(items: MutableList<InspectionTier>): MutableList<InspectionTierView> {
    val views = mutableListOf<InspectionTierView>()
    for (item in items) {
        views.add(toInspectionTierView(item))
    }
    return views
}

fun sumInspectionTierView(view: InspectionTierView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedDurationMinutes.toDouble()
}
