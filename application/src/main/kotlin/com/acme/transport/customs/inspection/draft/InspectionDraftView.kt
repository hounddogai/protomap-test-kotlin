package com.acme.transport.customs.inspection.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection draft. */
data class InspectionDraftView(
    var codeText: String,
    var name: String,
    var loadKg: String,
    var laneText: String,
    var pinned: String,
    var source: String,
    var ceiling: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toInspectionDraftView(item: InspectionDraft): InspectionDraftView {
    return InspectionDraftView(
        codeText = item.code,
        name = truncate(item.name, 80),
        loadKg = "%.2f".format(item.loadKg),
        laneText = item.lane,
        pinned = "${item.pinned}",
        source = item.source,
        ceiling = "%.2f".format(item.ceiling),
        windowDays = item.windowDays.toString(),
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
    val parsedCeiling = view.ceiling.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedLoadKg + parsedCeiling + parsedWindowDays.toDouble()
}
