package com.acme.transport.terminal.traileryard.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict trailer yard checkpoint. */
data class StrictTrailerYardCheckpointView(
    var code: String,
    var titleText: String,
    var discountText: String,
    var carrier: String,
    var etaMinutes: String,
    var phone: String,
    var sequence: String,
    var priority: String,
    var batchSize: String,
    var lane: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toStrictTrailerYardCheckpointView(item: StrictTrailerYardCheckpoint): StrictTrailerYardCheckpointView {
    return StrictTrailerYardCheckpointView(
        code = truncate(item.code, 120),
        titleText = truncate(item.title, 80),
        discountText = "%.2f".format(item.discount),
        carrier = truncate(item.carrier, 40),
        etaMinutes = item.etaMinutes.toString(),
        phone = item.phone,
        sequence = item.sequence.toString(),
        priority = item.priority.toString(),
        batchSize = item.batchSize.toString(),
        lane = truncate(item.lane, 120),
        summaryText = truncate(item.summary, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTrailerYardCheckpointViews(items: MutableList<StrictTrailerYardCheckpoint>): MutableList<StrictTrailerYardCheckpointView> {
    val views = mutableListOf<StrictTrailerYardCheckpointView>()
    for (item in items) {
        views.add(toStrictTrailerYardCheckpointView(item))
    }
    return views
}

fun sumStrictTrailerYardCheckpointView(view: StrictTrailerYardCheckpointView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedDiscount + parsedEtaMinutes.toDouble() + parsedSequence.toDouble()
}
