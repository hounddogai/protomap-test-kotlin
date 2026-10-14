package com.acme.transport.terminal.traileryard.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard summary. */
data class TrailerYardSummaryView(
    var codeText: String,
    var nameText: String,
    var etaMinutes: String,
    var ownerId: String,
    var notesText: String,
    var statusLabel: String,
)

fun toTrailerYardSummaryView(item: TrailerYardSummary): TrailerYardSummaryView {
    return TrailerYardSummaryView(
        codeText = item.code,
        nameText = truncate(item.name, 40),
        etaMinutes = item.etaMinutes.toString(),
        ownerId = item.ownerId,
        notesText = truncate(item.notes, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardSummaryViews(items: MutableList<TrailerYardSummary>): MutableList<TrailerYardSummaryView> {
    val views = mutableListOf<TrailerYardSummaryView>()
    for (item in items) {
        views.add(toTrailerYardSummaryView(item))
    }
    return views
}

fun sumTrailerYardSummaryView(view: TrailerYardSummaryView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEtaMinutes.toDouble()
}
