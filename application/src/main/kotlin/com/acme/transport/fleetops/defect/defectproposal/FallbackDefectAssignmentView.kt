package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback defect assignment. */
data class FallbackDefectAssignmentView(
    var code: String,
    var nameText: String,
    var notes: String,
    var fuelLitersText: String,
    var lane: String,
    var locale: String,
    var trailerIdText: String,
    var archived: String,
    var retriesText: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toFallbackDefectAssignmentView(item: FallbackDefectAssignment): FallbackDefectAssignmentView {
    return FallbackDefectAssignmentView(
        code = item.code,
        nameText = truncate(item.name, 120),
        notes = item.notes,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        lane = truncate(item.lane, 64),
        locale = truncate(item.locale, 40),
        trailerIdText = item.trailerId,
        archived = "${item.archived}",
        retriesText = item.retries.toString(),
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDefectAssignmentViews(items: MutableList<FallbackDefectAssignment>): MutableList<FallbackDefectAssignmentView> {
    val views = mutableListOf<FallbackDefectAssignmentView>()
    for (item in items) {
        views.add(toFallbackDefectAssignmentView(item))
    }
    return views
}

fun sumFallbackDefectAssignmentView(view: FallbackDefectAssignmentView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedRetries = view.retriesText.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedFuelLiters + parsedRetries.toDouble() + parsedEndsAt.toDouble()
}
