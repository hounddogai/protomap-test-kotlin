package com.acme.transport.crewing.qualification.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched qualification milestone. */
data class BatchedQualificationMilestoneView(
    var code: String,
    var titleText: String,
    var fuelLiters: String,
    var trailerIdText: String,
    var timezone: String,
    var discount: String,
    var statusLabel: String,
)

fun toBatchedQualificationMilestoneView(item: BatchedQualificationMilestone): BatchedQualificationMilestoneView {
    return BatchedQualificationMilestoneView(
        code = item.code,
        titleText = item.title,
        fuelLiters = "%.2f".format(item.fuelLiters),
        trailerIdText = truncate(item.trailerId, 120),
        timezone = item.timezone,
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedQualificationMilestoneViews(items: MutableList<BatchedQualificationMilestone>): MutableList<BatchedQualificationMilestoneView> {
    val views = mutableListOf<BatchedQualificationMilestoneView>()
    for (item in items) {
        views.add(toBatchedQualificationMilestoneView(item))
    }
    return views
}

fun sumBatchedQualificationMilestoneView(view: BatchedQualificationMilestoneView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedFuelLiters + parsedDiscount
}
