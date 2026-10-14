package com.acme.transport.emissions.evcharge.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched ev charge schedule. */
data class BatchedEvChargeScheduleView(
    var code: String,
    var name: String,
    var ownerId: String,
    var laneText: String,
    var source: String,
    var notesText: String,
    var attempts: String,
    var odometer: String,
    var country: String,
    var segmentText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toBatchedEvChargeScheduleView(item: BatchedEvChargeSchedule): BatchedEvChargeScheduleView {
    return BatchedEvChargeScheduleView(
        code = item.code,
        name = truncate(item.name, 120),
        ownerId = item.ownerId,
        laneText = truncate(item.lane, 80),
        source = truncate(item.source, 80),
        notesText = item.notes,
        attempts = item.attempts.toString(),
        odometer = item.odometer.toString(),
        country = item.country,
        segmentText = item.segment,
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedEvChargeScheduleViews(items: MutableList<BatchedEvChargeSchedule>): MutableList<BatchedEvChargeScheduleView> {
    val views = mutableListOf<BatchedEvChargeScheduleView>()
    for (item in items) {
        views.add(toBatchedEvChargeScheduleView(item))
    }
    return views
}

fun sumBatchedEvChargeScheduleView(view: BatchedEvChargeScheduleView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedScore = view.scoreText.toDouble()
    return parsedAttempts.toDouble() + parsedOdometer.toDouble() + parsedScore
}
