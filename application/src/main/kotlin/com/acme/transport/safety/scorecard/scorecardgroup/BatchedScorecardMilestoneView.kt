package com.acme.transport.safety.scorecard.scorecardgroup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched scorecard milestone. */
data class BatchedScorecardMilestoneView(
    var codeText: String,
    var title: String,
    var capacityText: String,
    var summary: String,
    var retries: String,
    var driverId: String,
    var pinned: String,
    var locale: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toBatchedScorecardMilestoneView(item: BatchedScorecardMilestone): BatchedScorecardMilestoneView {
    return BatchedScorecardMilestoneView(
        codeText = item.code,
        title = item.title,
        capacityText = item.capacity.toString(),
        summary = item.summary,
        retries = item.retries.toString(),
        driverId = truncate(item.driverId, 64),
        pinned = "${item.pinned}",
        locale = item.locale,
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedScorecardMilestoneViews(items: MutableList<BatchedScorecardMilestone>): MutableList<BatchedScorecardMilestoneView> {
    val views = mutableListOf<BatchedScorecardMilestoneView>()
    for (item in items) {
        views.add(toBatchedScorecardMilestoneView(item))
    }
    return views
}

fun sumBatchedScorecardMilestoneView(view: BatchedScorecardMilestoneView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedCapacity.toDouble() + parsedRetries.toDouble() + parsedUpdatedAt.toDouble()
}
