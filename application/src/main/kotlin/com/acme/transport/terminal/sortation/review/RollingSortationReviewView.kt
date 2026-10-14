package com.acme.transport.terminal.sortation.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling sortation review. */
data class RollingSortationReviewView(
    var code: String,
    var label: String,
    var batchSize: String,
    var loadKgText: String,
    var durationMinutes: String,
    var retries: String,
    var summary: String,
    var etaMinutesText: String,
    var version: String,
    var statusLabel: String,
)

fun toRollingSortationReviewView(item: RollingSortationReview): RollingSortationReviewView {
    return RollingSortationReviewView(
        code = item.code,
        label = item.label,
        batchSize = item.batchSize.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        durationMinutes = item.durationMinutes.toString(),
        retries = item.retries.toString(),
        summary = truncate(item.summary, 120),
        etaMinutesText = item.etaMinutes.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingSortationReviewViews(items: MutableList<RollingSortationReview>): MutableList<RollingSortationReviewView> {
    val views = mutableListOf<RollingSortationReviewView>()
    for (item in items) {
        views.add(toRollingSortationReviewView(item))
    }
    return views
}

fun sumRollingSortationReviewView(view: RollingSortationReviewView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedBatchSize.toDouble() + parsedLoadKg + parsedDurationMinutes.toDouble()
}
