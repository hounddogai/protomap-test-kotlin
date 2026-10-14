package com.acme.transport.lastmile.locker.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched locker plan. */
data class BatchedLockerPlanView(
    var code: String,
    var label: String,
    var dock: String,
    var trailerIdText: String,
    var attempts: String,
    var lane: String,
    var stopCount: String,
    var expiresAt: String,
    var shiftCode: String,
    var unit: String,
    var active: String,
    var statusLabel: String,
)

fun toBatchedLockerPlanView(item: BatchedLockerPlan): BatchedLockerPlanView {
    return BatchedLockerPlanView(
        code = item.code,
        label = item.label,
        dock = item.dock,
        trailerIdText = truncate(item.trailerId, 40),
        attempts = item.attempts.toString(),
        lane = item.lane,
        stopCount = item.stopCount.toString(),
        expiresAt = item.expiresAt.toString(),
        shiftCode = item.shiftCode,
        unit = truncate(item.unit, 40),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedLockerPlanViews(items: MutableList<BatchedLockerPlan>): MutableList<BatchedLockerPlanView> {
    val views = mutableListOf<BatchedLockerPlanView>()
    for (item in items) {
        views.add(toBatchedLockerPlanView(item))
    }
    return views
}

fun sumBatchedLockerPlanView(view: BatchedLockerPlanView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedAttempts.toDouble() + parsedStopCount.toDouble() + parsedExpiresAt.toDouble()
}
