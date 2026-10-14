package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled violation draft. */
data class ScheduledViolationDraftView(
    var codeText: String,
    var name: String,
    var active: String,
    var timezoneText: String,
    var dockText: String,
    var trailerId: String,
    var archivedText: String,
    var notesText: String,
    var contactEmailText: String,
    var weight: String,
    var driverId: String,
    var statusLabel: String,
)

fun toScheduledViolationDraftView(item: ScheduledViolationDraft): ScheduledViolationDraftView {
    return ScheduledViolationDraftView(
        codeText = truncate(item.code, 80),
        name = item.name,
        active = "${item.active}",
        timezoneText = truncate(item.timezone, 120),
        dockText = item.dock,
        trailerId = item.trailerId,
        archivedText = "${item.archived}",
        notesText = item.notes,
        contactEmailText = truncate(item.contactEmail, 120),
        weight = "%.2f".format(item.weight),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledViolationDraftViews(items: MutableList<ScheduledViolationDraft>): MutableList<ScheduledViolationDraftView> {
    val views = mutableListOf<ScheduledViolationDraftView>()
    for (item in items) {
        views.add(toScheduledViolationDraftView(item))
    }
    return views
}

fun sumScheduledViolationDraftView(view: ScheduledViolationDraftView): Double {
    val parsedWeight = view.weight.toDouble()
    return parsedWeight
}
