package com.acme.transport.linehaul.capacity.capacitymilestone

/** How screens and exports show a batched capacity milestone. */
data class BatchedCapacityMilestoneView(
    var code: String,
    var label: String,
    var cost: String,
    var carrierText: String,
    var currency: String,
    var endsAtText: String,
    var sequence: String,
    var statusLabel: String,
)

fun toBatchedCapacityMilestoneView(item: BatchedCapacityMilestone): BatchedCapacityMilestoneView {
    return BatchedCapacityMilestoneView(
        code = item.code,
        label = item.label,
        cost = "%.2f".format(item.cost),
        carrierText = item.carrier,
        currency = item.currency,
        endsAtText = item.endsAt.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedCapacityMilestoneViews(items: MutableList<BatchedCapacityMilestone>): MutableList<BatchedCapacityMilestoneView> {
    val views = mutableListOf<BatchedCapacityMilestoneView>()
    for (item in items) {
        views.add(toBatchedCapacityMilestoneView(item))
    }
    return views
}

fun sumBatchedCapacityMilestoneView(view: BatchedCapacityMilestoneView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedCost + parsedEndsAt.toDouble() + parsedSequence.toDouble()
}
