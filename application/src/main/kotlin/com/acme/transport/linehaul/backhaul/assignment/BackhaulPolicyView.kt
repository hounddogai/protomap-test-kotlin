package com.acme.transport.linehaul.backhaul.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a backhaul policy. */
data class BackhaulPolicyView(
    var codeText: String,
    var titleText: String,
    var priority: String,
    var sequence: String,
    var depot: String,
    var startsAt: String,
    var unit: String,
    var segmentText: String,
    var reviewer: String,
    var limit: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toBackhaulPolicyView(item: BackhaulPolicy): BackhaulPolicyView {
    return BackhaulPolicyView(
        codeText = truncate(item.code, 40),
        titleText = truncate(item.title, 40),
        priority = item.priority.toString(),
        sequence = item.sequence.toString(),
        depot = item.depot,
        startsAt = item.startsAt.toString(),
        unit = item.unit,
        segmentText = truncate(item.segment, 80),
        reviewer = truncate(item.reviewer, 80),
        limit = item.limit.toString(),
        trailerId = truncate(item.trailerId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulPolicyViews(items: MutableList<BackhaulPolicy>): MutableList<BackhaulPolicyView> {
    val views = mutableListOf<BackhaulPolicyView>()
    for (item in items) {
        views.add(toBackhaulPolicyView(item))
    }
    return views
}

fun sumBackhaulPolicyView(view: BackhaulPolicyView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedPriority.toDouble() + parsedSequence.toDouble() + parsedStartsAt.toDouble()
}
