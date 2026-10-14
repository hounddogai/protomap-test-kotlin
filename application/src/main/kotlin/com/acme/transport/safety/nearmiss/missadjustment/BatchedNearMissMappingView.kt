package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched near miss mapping. */
data class BatchedNearMissMappingView(
    var code: String,
    var nameText: String,
    var reviewer: String,
    var externalRef: String,
    var floorValue: String,
    var discount: String,
    var visible: String,
    var statusLabel: String,
)

fun toBatchedNearMissMappingView(item: BatchedNearMissMapping): BatchedNearMissMappingView {
    return BatchedNearMissMappingView(
        code = truncate(item.code, 120),
        nameText = item.name,
        reviewer = item.reviewer,
        externalRef = item.externalRef,
        floorValue = "%.2f".format(item.floorValue),
        discount = "%.2f".format(item.discount),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedNearMissMappingViews(items: MutableList<BatchedNearMissMapping>): MutableList<BatchedNearMissMappingView> {
    val views = mutableListOf<BatchedNearMissMappingView>()
    for (item in items) {
        views.add(toBatchedNearMissMappingView(item))
    }
    return views
}

fun sumBatchedNearMissMappingView(view: BatchedNearMissMappingView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedFloorValue + parsedDiscount
}
