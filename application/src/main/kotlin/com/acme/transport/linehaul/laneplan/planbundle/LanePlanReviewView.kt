package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan review. */
data class LanePlanReviewView(
    var codeText: String,
    var label: String,
    var priority: String,
    var attemptsText: String,
    var archived: String,
    var depot: String,
    var batchSize: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toLanePlanReviewView(item: LanePlanReview): LanePlanReviewView {
    return LanePlanReviewView(
        codeText = item.code,
        label = truncate(item.label, 120),
        priority = item.priority.toString(),
        attemptsText = item.attempts.toString(),
        archived = "${item.archived}",
        depot = truncate(item.depot, 80),
        batchSize = item.batchSize.toString(),
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanReviewViews(items: MutableList<LanePlanReview>): MutableList<LanePlanReviewView> {
    val views = mutableListOf<LanePlanReviewView>()
    for (item in items) {
        views.add(toLanePlanReviewView(item))
    }
    return views
}

fun sumLanePlanReviewView(view: LanePlanReviewView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedPriority.toDouble() + parsedAttempts.toDouble() + parsedBatchSize.toDouble()
}
