package com.acme.transport.crewing.roster.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched roster adjustment. */
data class BatchedRosterAdjustmentView(
    var code: String,
    var label: String,
    var priority: String,
    var shiftCodeText: String,
    var reviewer: String,
    var notesText: String,
    var country: String,
    var statusLabel: String,
)

fun toBatchedRosterAdjustmentView(item: BatchedRosterAdjustment): BatchedRosterAdjustmentView {
    return BatchedRosterAdjustmentView(
        code = truncate(item.code, 80),
        label = item.label,
        priority = item.priority.toString(),
        shiftCodeText = truncate(item.shiftCode, 64),
        reviewer = item.reviewer,
        notesText = item.notes,
        country = truncate(item.country, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedRosterAdjustmentViews(items: MutableList<BatchedRosterAdjustment>): MutableList<BatchedRosterAdjustmentView> {
    val views = mutableListOf<BatchedRosterAdjustmentView>()
    for (item in items) {
        views.add(toBatchedRosterAdjustmentView(item))
    }
    return views
}

fun sumBatchedRosterAdjustmentView(view: BatchedRosterAdjustmentView): Double {
    val parsedPriority = view.priority.toInt()
    return parsedPriority.toDouble()
}
