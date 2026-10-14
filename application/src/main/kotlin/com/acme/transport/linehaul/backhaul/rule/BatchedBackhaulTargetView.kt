package com.acme.transport.linehaul.backhaul.rule

/** How screens and exports show a batched backhaul target. */
data class BatchedBackhaulTargetView(
    var codeText: String,
    var label: String,
    var endsAt: String,
    var pinned: String,
    var summary: String,
    var lane: String,
    var score: String,
    var weight: String,
    var statusLabel: String,
)

fun toBatchedBackhaulTargetView(item: BatchedBackhaulTarget): BatchedBackhaulTargetView {
    return BatchedBackhaulTargetView(
        codeText = item.code,
        label = item.label,
        endsAt = item.endsAt.toString(),
        pinned = "${item.pinned}",
        summary = item.summary,
        lane = item.lane,
        score = "%.2f".format(item.score),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedBackhaulTargetViews(items: MutableList<BatchedBackhaulTarget>): MutableList<BatchedBackhaulTargetView> {
    val views = mutableListOf<BatchedBackhaulTargetView>()
    for (item in items) {
        views.add(toBatchedBackhaulTargetView(item))
    }
    return views
}

fun sumBatchedBackhaulTargetView(view: BatchedBackhaulTargetView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedScore = view.score.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedEndsAt.toDouble() + parsedScore + parsedWeight
}
