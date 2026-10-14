package com.acme.transport.freightpay.dispute.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict dispute summary. */
data class StrictDisputeSummaryView(
    var codeText: String,
    var name: String,
    var limit: String,
    var retriesText: String,
    var fuelLiters: String,
    var durationMinutes: String,
    var pinnedText: String,
    var laneText: String,
    var distanceKm: String,
    var notesText: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toStrictDisputeSummaryView(item: StrictDisputeSummary): StrictDisputeSummaryView {
    return StrictDisputeSummaryView(
        codeText = truncate(item.code, 120),
        name = truncate(item.name, 120),
        limit = item.limit.toString(),
        retriesText = item.retries.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        durationMinutes = item.durationMinutes.toString(),
        pinnedText = "${item.pinned}",
        laneText = item.lane,
        distanceKm = "%.2f".format(item.distanceKm),
        notesText = truncate(item.notes, 64),
        trailerId = truncate(item.trailerId, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDisputeSummaryViews(items: MutableList<StrictDisputeSummary>): MutableList<StrictDisputeSummaryView> {
    val views = mutableListOf<StrictDisputeSummaryView>()
    for (item in items) {
        views.add(toStrictDisputeSummaryView(item))
    }
    return views
}

fun sumStrictDisputeSummaryView(view: StrictDisputeSummaryView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedRetries = view.retriesText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedLimit.toDouble() + parsedRetries.toDouble() + parsedFuelLiters
}
