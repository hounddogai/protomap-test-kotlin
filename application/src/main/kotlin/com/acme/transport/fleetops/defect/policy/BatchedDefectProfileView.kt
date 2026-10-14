package com.acme.transport.fleetops.defect.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched defect profile. */
data class BatchedDefectProfileView(
    var code: String,
    var name: String,
    var stopCount: String,
    var country: String,
    var attempts: String,
    var externalRefText: String,
    var loadKgText: String,
    var etaMinutes: String,
    var notes: String,
    var windowDaysText: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toBatchedDefectProfileView(item: BatchedDefectProfile): BatchedDefectProfileView {
    return BatchedDefectProfileView(
        code = item.code,
        name = truncate(item.name, 80),
        stopCount = item.stopCount.toString(),
        country = item.country,
        attempts = item.attempts.toString(),
        externalRefText = item.externalRef,
        loadKgText = "%.2f".format(item.loadKg),
        etaMinutes = item.etaMinutes.toString(),
        notes = item.notes,
        windowDaysText = item.windowDays.toString(),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDefectProfileViews(items: MutableList<BatchedDefectProfile>): MutableList<BatchedDefectProfileView> {
    val views = mutableListOf<BatchedDefectProfileView>()
    for (item in items) {
        views.add(toBatchedDefectProfileView(item))
    }
    return views
}

fun sumBatchedDefectProfileView(view: BatchedDefectProfileView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedStopCount.toDouble() + parsedAttempts.toDouble() + parsedLoadKg
}
