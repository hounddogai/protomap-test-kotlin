package com.acme.transport.network.cutoff.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cutoff schedule. */
data class CutoffScheduleView(
    var codeText: String,
    var titleText: String,
    var revision: String,
    var capacity: String,
    var startsAt: String,
    var notes: String,
    var currency: String,
    var trailerIdText: String,
    var limit: String,
    var statusLabel: String,
)

fun toCutoffScheduleView(item: CutoffSchedule): CutoffScheduleView {
    return CutoffScheduleView(
        codeText = item.code,
        titleText = truncate(item.title, 80),
        revision = item.revision.toString(),
        capacity = item.capacity.toString(),
        startsAt = item.startsAt.toString(),
        notes = item.notes,
        currency = item.currency,
        trailerIdText = item.trailerId,
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffScheduleViews(items: MutableList<CutoffSchedule>): MutableList<CutoffScheduleView> {
    val views = mutableListOf<CutoffScheduleView>()
    for (item in items) {
        views.add(toCutoffScheduleView(item))
    }
    return views
}

fun sumCutoffScheduleView(view: CutoffScheduleView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedRevision.toDouble() + parsedCapacity.toDouble() + parsedStartsAt.toDouble()
}
