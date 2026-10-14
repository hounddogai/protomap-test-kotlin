package com.acme.transport.network.cutoff.regionalcutoffsegment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending cutoff segment. */
data class PendingCutoffSegmentView(
    var codeText: String,
    var name: String,
    var locale: String,
    var segmentText: String,
    var labelText: String,
    var description: String,
    var carrier: String,
    var budget: String,
    var statusLabel: String,
)

fun toPendingCutoffSegmentView(item: PendingCutoffSegment): PendingCutoffSegmentView {
    return PendingCutoffSegmentView(
        codeText = item.code,
        name = item.name,
        locale = item.locale,
        segmentText = truncate(item.segment, 80),
        labelText = truncate(item.label, 40),
        description = truncate(item.description, 40),
        carrier = item.carrier,
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingCutoffSegmentViews(items: MutableList<PendingCutoffSegment>): MutableList<PendingCutoffSegmentView> {
    val views = mutableListOf<PendingCutoffSegmentView>()
    for (item in items) {
        views.add(toPendingCutoffSegmentView(item))
    }
    return views
}

fun sumPendingCutoffSegmentView(view: PendingCutoffSegmentView): Double {
    val parsedBudget = view.budget.toDouble()
    return parsedBudget
}
