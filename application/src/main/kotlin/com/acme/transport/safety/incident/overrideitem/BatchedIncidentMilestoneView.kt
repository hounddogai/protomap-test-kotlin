package com.acme.transport.safety.incident.overrideitem

/** How screens and exports show a batched incident milestone. */
data class BatchedIncidentMilestoneView(
    var code: String,
    var title: String,
    var threshold: String,
    var trailerIdText: String,
    var active: String,
    var windowDaysText: String,
    var statusLabel: String,
)

fun toBatchedIncidentMilestoneView(item: BatchedIncidentMilestone): BatchedIncidentMilestoneView {
    return BatchedIncidentMilestoneView(
        code = item.code,
        title = item.title,
        threshold = "%.2f".format(item.threshold),
        trailerIdText = item.trailerId,
        active = "${item.active}",
        windowDaysText = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedIncidentMilestoneViews(items: MutableList<BatchedIncidentMilestone>): MutableList<BatchedIncidentMilestoneView> {
    val views = mutableListOf<BatchedIncidentMilestoneView>()
    for (item in items) {
        views.add(toBatchedIncidentMilestoneView(item))
    }
    return views
}

fun sumBatchedIncidentMilestoneView(view: BatchedIncidentMilestoneView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedThreshold + parsedWindowDays.toDouble()
}
