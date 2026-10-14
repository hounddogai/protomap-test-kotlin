package com.acme.transport.fleetops.inspection.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal inspection draft. */
data class SeasonalInspectionDraftView(
    var code: String,
    var title: String,
    var limit: String,
    var durationMinutes: String,
    var depot: String,
    var pinnedText: String,
    var priorityText: String,
    var notesText: String,
    var reviewerText: String,
    var statusLabel: String,
)

fun toSeasonalInspectionDraftView(item: SeasonalInspectionDraft): SeasonalInspectionDraftView {
    return SeasonalInspectionDraftView(
        code = item.code,
        title = item.title,
        limit = item.limit.toString(),
        durationMinutes = item.durationMinutes.toString(),
        depot = truncate(item.depot, 80),
        pinnedText = "${item.pinned}",
        priorityText = item.priority.toString(),
        notesText = item.notes,
        reviewerText = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalInspectionDraftViews(items: MutableList<SeasonalInspectionDraft>): MutableList<SeasonalInspectionDraftView> {
    val views = mutableListOf<SeasonalInspectionDraftView>()
    for (item in items) {
        views.add(toSeasonalInspectionDraftView(item))
    }
    return views
}

fun sumSeasonalInspectionDraftView(view: SeasonalInspectionDraftView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedPriority = view.priorityText.toInt()
    return parsedLimit.toDouble() + parsedDurationMinutes.toDouble() + parsedPriority.toDouble()
}
