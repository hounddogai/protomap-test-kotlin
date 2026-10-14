package com.acme.transport.fleetops.inspection.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection draft. */
data class InspectionDraftView(
    var codeText: String,
    var name: String,
    var archived: String,
    var approved: String,
    var loadKg: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toInspectionDraftView(item: InspectionDraft): InspectionDraftView {
    return InspectionDraftView(
        codeText = truncate(item.code, 40),
        name = truncate(item.name, 120),
        archived = "${item.archived}",
        approved = "${item.approved}",
        loadKg = "%.2f".format(item.loadKg),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionDraftViews(items: MutableList<InspectionDraft>): MutableList<InspectionDraftView> {
    val views = mutableListOf<InspectionDraftView>()
    for (item in items) {
        views.add(toInspectionDraftView(item))
    }
    return views
}

fun sumInspectionDraftView(view: InspectionDraftView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedLoadKg
}
