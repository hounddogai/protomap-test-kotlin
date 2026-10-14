package com.acme.transport.safety.incident.proposal

/** How screens and exports show a batched incident proposal. */
data class BatchedIncidentProposalView(
    var codeText: String,
    var label: String,
    var weight: String,
    var distanceKmText: String,
    var phone: String,
    var windowDays: String,
    var priorityText: String,
    var source: String,
    var trailerId: String,
    var maximum: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toBatchedIncidentProposalView(item: BatchedIncidentProposal): BatchedIncidentProposalView {
    return BatchedIncidentProposalView(
        codeText = item.code,
        label = item.label,
        weight = "%.2f".format(item.weight),
        distanceKmText = "%.2f".format(item.distanceKm),
        phone = item.phone,
        windowDays = item.windowDays.toString(),
        priorityText = item.priority.toString(),
        source = item.source,
        trailerId = item.trailerId,
        maximum = "%.2f".format(item.maximum),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedIncidentProposalViews(items: MutableList<BatchedIncidentProposal>): MutableList<BatchedIncidentProposalView> {
    val views = mutableListOf<BatchedIncidentProposalView>()
    for (item in items) {
        views.add(toBatchedIncidentProposalView(item))
    }
    return views
}

fun sumBatchedIncidentProposalView(view: BatchedIncidentProposalView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedWeight + parsedDistanceKm + parsedWindowDays.toDouble()
}
