package com.acme.transport.network.transittime.scheduledtimebatch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled transit time batch. */
data class ScheduledTransitTimeBatchView(
    var code: String,
    var nameText: String,
    var phone: String,
    var sequence: String,
    var quantity: String,
    var score: String,
    var ratio: String,
    var attempts: String,
    var archivedText: String,
    var startsAtText: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toScheduledTransitTimeBatchView(item: ScheduledTransitTimeBatch): ScheduledTransitTimeBatchView {
    return ScheduledTransitTimeBatchView(
        code = item.code,
        nameText = item.name,
        phone = truncate(item.phone, 64),
        sequence = item.sequence.toString(),
        quantity = item.quantity.toString(),
        score = "%.2f".format(item.score),
        ratio = "%.2f".format(item.ratio),
        attempts = item.attempts.toString(),
        archivedText = "${item.archived}",
        startsAtText = item.startsAt.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledTransitTimeBatchViews(items: MutableList<ScheduledTransitTimeBatch>): MutableList<ScheduledTransitTimeBatchView> {
    val views = mutableListOf<ScheduledTransitTimeBatchView>()
    for (item in items) {
        views.add(toScheduledTransitTimeBatchView(item))
    }
    return views
}

fun sumScheduledTransitTimeBatchView(view: ScheduledTransitTimeBatchView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedScore = view.score.toDouble()
    return parsedSequence.toDouble() + parsedQuantity.toDouble() + parsedScore
}
