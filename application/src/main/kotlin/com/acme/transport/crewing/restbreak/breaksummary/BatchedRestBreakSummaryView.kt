package com.acme.transport.crewing.restbreak.breaksummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched rest break summary. */
data class BatchedRestBreakSummaryView(
    var code: String,
    var nameText: String,
    var discountText: String,
    var contactEmailText: String,
    var priorityText: String,
    var statusLabel: String,
)

fun toBatchedRestBreakSummaryView(item: BatchedRestBreakSummary): BatchedRestBreakSummaryView {
    return BatchedRestBreakSummaryView(
        code = truncate(item.code, 80),
        nameText = item.name,
        discountText = "%.2f".format(item.discount),
        contactEmailText = item.contactEmail,
        priorityText = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedRestBreakSummaryViews(items: MutableList<BatchedRestBreakSummary>): MutableList<BatchedRestBreakSummaryView> {
    val views = mutableListOf<BatchedRestBreakSummaryView>()
    for (item in items) {
        views.add(toBatchedRestBreakSummaryView(item))
    }
    return views
}

fun sumBatchedRestBreakSummaryView(view: BatchedRestBreakSummaryView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedPriority = view.priorityText.toInt()
    return parsedDiscount + parsedPriority.toDouble()
}
