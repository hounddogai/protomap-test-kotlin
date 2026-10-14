package com.acme.transport.terminal.traileryard.manifest

/** How screens and exports show a trailer yard checkpoint. */
data class TrailerYardCheckpointView(
    var code: String,
    var labelText: String,
    var pinned: String,
    var loadKg: String,
    var attempts: String,
    var carrier: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toTrailerYardCheckpointView(item: TrailerYardCheckpoint): TrailerYardCheckpointView {
    return TrailerYardCheckpointView(
        code = item.code,
        labelText = item.label,
        pinned = "${item.pinned}",
        loadKg = "%.2f".format(item.loadKg),
        attempts = item.attempts.toString(),
        carrier = item.carrier,
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardCheckpointViews(items: MutableList<TrailerYardCheckpoint>): MutableList<TrailerYardCheckpointView> {
    val views = mutableListOf<TrailerYardCheckpointView>()
    for (item in items) {
        views.add(toTrailerYardCheckpointView(item))
    }
    return views
}

fun sumTrailerYardCheckpointView(view: TrailerYardCheckpointView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedAttempts = view.attempts.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedLoadKg + parsedAttempts.toDouble() + parsedCreatedAt.toDouble()
}
