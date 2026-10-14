package com.acme.transport.linehaul.capacity.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched capacity group. */
data class BatchedCapacityGroupView(
    var codeText: String,
    var label: String,
    var graceDays: String,
    var notesText: String,
    var dockText: String,
    var unit: String,
    var amountText: String,
    var margin: String,
    var routeCode: String,
    var endsAt: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toBatchedCapacityGroupView(item: BatchedCapacityGroup): BatchedCapacityGroupView {
    return BatchedCapacityGroupView(
        codeText = item.code,
        label = truncate(item.label, 40),
        graceDays = item.graceDays.toString(),
        notesText = truncate(item.notes, 120),
        dockText = item.dock,
        unit = truncate(item.unit, 40),
        amountText = "%.2f".format(item.amount),
        margin = "%.2f".format(item.margin),
        routeCode = item.routeCode,
        endsAt = item.endsAt.toString(),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedCapacityGroupViews(items: MutableList<BatchedCapacityGroup>): MutableList<BatchedCapacityGroupView> {
    val views = mutableListOf<BatchedCapacityGroupView>()
    for (item in items) {
        views.add(toBatchedCapacityGroupView(item))
    }
    return views
}

fun sumBatchedCapacityGroupView(view: BatchedCapacityGroupView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedAmount = view.amountText.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedGraceDays.toDouble() + parsedAmount + parsedMargin
}
