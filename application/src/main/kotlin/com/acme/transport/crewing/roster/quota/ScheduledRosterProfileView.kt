package com.acme.transport.crewing.roster.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled roster profile. */
data class ScheduledRosterProfileView(
    var code: String,
    var label: String,
    var shiftCode: String,
    var phoneText: String,
    var driverId: String,
    var expiresAt: String,
    var description: String,
    var endsAtText: String,
    var reviewer: String,
    var archived: String,
    var statusLabel: String,
)

fun toScheduledRosterProfileView(item: ScheduledRosterProfile): ScheduledRosterProfileView {
    return ScheduledRosterProfileView(
        code = item.code,
        label = item.label,
        shiftCode = truncate(item.shiftCode, 40),
        phoneText = item.phone,
        driverId = truncate(item.driverId, 120),
        expiresAt = item.expiresAt.toString(),
        description = item.description,
        endsAtText = item.endsAt.toString(),
        reviewer = item.reviewer,
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledRosterProfileViews(items: MutableList<ScheduledRosterProfile>): MutableList<ScheduledRosterProfileView> {
    val views = mutableListOf<ScheduledRosterProfileView>()
    for (item in items) {
        views.add(toScheduledRosterProfileView(item))
    }
    return views
}

fun sumScheduledRosterProfileView(view: ScheduledRosterProfileView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedExpiresAt.toDouble() + parsedEndsAt.toDouble()
}
