package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss checkpoint. */
data class NearMissCheckpointView(
    var code: String,
    var label: String,
    var ownerIdText: String,
    var durationMinutes: String,
    var notesText: String,
    var startsAt: String,
    var margin: String,
    var cost: String,
    var endsAtText: String,
    var pinnedText: String,
    var amount: String,
    var statusLabel: String,
)

fun toNearMissCheckpointView(item: NearMissCheckpoint): NearMissCheckpointView {
    return NearMissCheckpointView(
        code = item.code,
        label = item.label,
        ownerIdText = item.ownerId,
        durationMinutes = item.durationMinutes.toString(),
        notesText = truncate(item.notes, 64),
        startsAt = item.startsAt.toString(),
        margin = "%.2f".format(item.margin),
        cost = "%.2f".format(item.cost),
        endsAtText = item.endsAt.toString(),
        pinnedText = "${item.pinned}",
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissCheckpointViews(items: MutableList<NearMissCheckpoint>): MutableList<NearMissCheckpointView> {
    val views = mutableListOf<NearMissCheckpointView>()
    for (item in items) {
        views.add(toNearMissCheckpointView(item))
    }
    return views
}

fun sumNearMissCheckpointView(view: NearMissCheckpointView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedDurationMinutes.toDouble() + parsedStartsAt.toDouble() + parsedMargin
}
