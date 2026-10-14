package com.acme.transport.terminal.crossdock.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy cross dock checkpoint. */
data class LegacyCrossDockCheckpointView(
    var code: String,
    var labelText: String,
    var lane: String,
    var pinned: String,
    var dock: String,
    var archived: String,
    var sourceText: String,
    var startsAt: String,
    var attempts: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toLegacyCrossDockCheckpointView(item: LegacyCrossDockCheckpoint): LegacyCrossDockCheckpointView {
    return LegacyCrossDockCheckpointView(
        code = item.code,
        labelText = truncate(item.label, 64),
        lane = item.lane,
        pinned = "${item.pinned}",
        dock = item.dock,
        archived = "${item.archived}",
        sourceText = item.source,
        startsAt = item.startsAt.toString(),
        attempts = item.attempts.toString(),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyCrossDockCheckpointViews(items: MutableList<LegacyCrossDockCheckpoint>): MutableList<LegacyCrossDockCheckpointView> {
    val views = mutableListOf<LegacyCrossDockCheckpointView>()
    for (item in items) {
        views.add(toLegacyCrossDockCheckpointView(item))
    }
    return views
}

fun sumLegacyCrossDockCheckpointView(view: LegacyCrossDockCheckpointView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedStartsAt.toDouble() + parsedAttempts.toDouble() + parsedCeiling
}
