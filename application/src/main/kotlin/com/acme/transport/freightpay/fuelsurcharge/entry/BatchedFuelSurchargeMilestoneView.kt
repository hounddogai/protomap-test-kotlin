package com.acme.transport.freightpay.fuelsurcharge.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched fuel surcharge milestone. */
data class BatchedFuelSurchargeMilestoneView(
    var codeText: String,
    var name: String,
    var titleText: String,
    var trailerId: String,
    var source: String,
    var discount: String,
    var externalRef: String,
    var weight: String,
    var statusLabel: String,
)

fun toBatchedFuelSurchargeMilestoneView(item: BatchedFuelSurchargeMilestone): BatchedFuelSurchargeMilestoneView {
    return BatchedFuelSurchargeMilestoneView(
        codeText = truncate(item.code, 120),
        name = item.name,
        titleText = item.title,
        trailerId = item.trailerId,
        source = item.source,
        discount = "%.2f".format(item.discount),
        externalRef = truncate(item.externalRef, 64),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedFuelSurchargeMilestoneViews(items: MutableList<BatchedFuelSurchargeMilestone>): MutableList<BatchedFuelSurchargeMilestoneView> {
    val views = mutableListOf<BatchedFuelSurchargeMilestoneView>()
    for (item in items) {
        views.add(toBatchedFuelSurchargeMilestoneView(item))
    }
    return views
}

fun sumBatchedFuelSurchargeMilestoneView(view: BatchedFuelSurchargeMilestoneView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedDiscount + parsedWeight
}
