package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched inspection slot. */
data class BatchedInspectionSlotView(
    var code: String,
    var title: String,
    var shiftCode: String,
    var discount: String,
    var capacity: String,
    var statusLabel: String,
)

fun toBatchedInspectionSlotView(item: BatchedInspectionSlot): BatchedInspectionSlotView {
    return BatchedInspectionSlotView(
        code = item.code,
        title = truncate(item.title, 80),
        shiftCode = truncate(item.shiftCode, 120),
        discount = "%.2f".format(item.discount),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedInspectionSlotViews(items: MutableList<BatchedInspectionSlot>): MutableList<BatchedInspectionSlotView> {
    val views = mutableListOf<BatchedInspectionSlotView>()
    for (item in items) {
        views.add(toBatchedInspectionSlotView(item))
    }
    return views
}

fun sumBatchedInspectionSlotView(view: BatchedInspectionSlotView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedDiscount + parsedCapacity.toDouble()
}
