package com.acme.transport.linehaul.tender.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender override. */
data class TenderOverrideView(
    var codeText: String,
    var name: String,
    var startsAt: String,
    var category: String,
    var attempts: String,
    var durationMinutesText: String,
    var description: String,
    var archivedText: String,
    var sequence: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toTenderOverrideView(item: TenderOverride): TenderOverrideView {
    return TenderOverrideView(
        codeText = truncate(item.code, 64),
        name = item.name,
        startsAt = item.startsAt.toString(),
        category = item.category,
        attempts = item.attempts.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        description = item.description,
        archivedText = "${item.archived}",
        sequence = item.sequence.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderOverrideViews(items: MutableList<TenderOverride>): MutableList<TenderOverrideView> {
    val views = mutableListOf<TenderOverrideView>()
    for (item in items) {
        views.add(toTenderOverrideView(item))
    }
    return views
}

fun sumTenderOverrideView(view: TenderOverrideView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedStartsAt.toDouble() + parsedAttempts.toDouble() + parsedDurationMinutes.toDouble()
}
