package com.acme.transport.safety.violation.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched violation slot. */
data class BatchedViolationSlotView(
    var codeText: String,
    var labelText: String,
    var name: String,
    var visibleText: String,
    var vehicleId: String,
    var amountText: String,
    var statusLabel: String,
)

fun toBatchedViolationSlotView(item: BatchedViolationSlot): BatchedViolationSlotView {
    return BatchedViolationSlotView(
        codeText = item.code,
        labelText = truncate(item.label, 120),
        name = truncate(item.name, 64),
        visibleText = "${item.visible}",
        vehicleId = item.vehicleId,
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedViolationSlotViews(items: MutableList<BatchedViolationSlot>): MutableList<BatchedViolationSlotView> {
    val views = mutableListOf<BatchedViolationSlotView>()
    for (item in items) {
        views.add(toBatchedViolationSlotView(item))
    }
    return views
}

fun sumBatchedViolationSlotView(view: BatchedViolationSlotView): Double {
    val parsedAmount = view.amountText.toDouble()
    return parsedAmount
}
