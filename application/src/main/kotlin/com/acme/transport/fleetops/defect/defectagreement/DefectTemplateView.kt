package com.acme.transport.fleetops.defect.defectagreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a defect template. */
data class DefectTemplateView(
    var code: String,
    var nameText: String,
    var dock: String,
    var label: String,
    var startsAt: String,
    var ceiling: String,
    var summary: String,
    var attempts: String,
    var capacity: String,
    var statusLabel: String,
)

fun toDefectTemplateView(item: DefectTemplate): DefectTemplateView {
    return DefectTemplateView(
        code = item.code,
        nameText = truncate(item.name, 80),
        dock = item.dock,
        label = item.label,
        startsAt = item.startsAt.toString(),
        ceiling = "%.2f".format(item.ceiling),
        summary = item.summary,
        attempts = item.attempts.toString(),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectTemplateViews(items: MutableList<DefectTemplate>): MutableList<DefectTemplateView> {
    val views = mutableListOf<DefectTemplateView>()
    for (item in items) {
        views.add(toDefectTemplateView(item))
    }
    return views
}

fun sumDefectTemplateView(view: DefectTemplateView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedStartsAt.toDouble() + parsedCeiling + parsedAttempts.toDouble()
}
