package com.acme.transport.terminal.traileryard.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard entry. */
data class TrailerYardEntryView(
    var code: String,
    var name: String,
    var contactEmail: String,
    var capacity: String,
    var revision: String,
    var trailerId: String,
    var etaMinutes: String,
    var lane: String,
    var score: String,
    var statusLabel: String,
)

fun toTrailerYardEntryView(item: TrailerYardEntry): TrailerYardEntryView {
    return TrailerYardEntryView(
        code = item.code,
        name = truncate(item.name, 40),
        contactEmail = truncate(item.contactEmail, 120),
        capacity = item.capacity.toString(),
        revision = item.revision.toString(),
        trailerId = item.trailerId,
        etaMinutes = item.etaMinutes.toString(),
        lane = truncate(item.lane, 80),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardEntryViews(items: MutableList<TrailerYardEntry>): MutableList<TrailerYardEntryView> {
    val views = mutableListOf<TrailerYardEntryView>()
    for (item in items) {
        views.add(toTrailerYardEntryView(item))
    }
    return views
}

fun sumTrailerYardEntryView(view: TrailerYardEntryView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedCapacity.toDouble() + parsedRevision.toDouble() + parsedEtaMinutes.toDouble()
}
