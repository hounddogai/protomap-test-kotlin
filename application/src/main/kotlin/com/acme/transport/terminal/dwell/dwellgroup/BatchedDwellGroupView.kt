package com.acme.transport.terminal.dwell.dwellgroup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched dwell group. */
data class BatchedDwellGroupView(
    var code: String,
    var name: String,
    var source: String,
    var revisionText: String,
    var endsAt: String,
    var discountText: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toBatchedDwellGroupView(item: BatchedDwellGroup): BatchedDwellGroupView {
    return BatchedDwellGroupView(
        code = item.code,
        name = item.name,
        source = item.source,
        revisionText = item.revision.toString(),
        endsAt = item.endsAt.toString(),
        discountText = "%.2f".format(item.discount),
        shiftCode = truncate(item.shiftCode, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedDwellGroupViews(items: MutableList<BatchedDwellGroup>): MutableList<BatchedDwellGroupView> {
    val views = mutableListOf<BatchedDwellGroupView>()
    for (item in items) {
        views.add(toBatchedDwellGroupView(item))
    }
    return views
}

fun sumBatchedDwellGroupView(view: BatchedDwellGroupView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedDiscount = view.discountText.toDouble()
    return parsedRevision.toDouble() + parsedEndsAt.toDouble() + parsedDiscount
}
