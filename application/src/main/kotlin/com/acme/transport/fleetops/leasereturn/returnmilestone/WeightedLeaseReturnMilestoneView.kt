package com.acme.transport.fleetops.leasereturn.returnmilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted lease return milestone. */
data class WeightedLeaseReturnMilestoneView(
    var code: String,
    var title: String,
    var channel: String,
    var quantityText: String,
    var approvedText: String,
    var version: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toWeightedLeaseReturnMilestoneView(item: WeightedLeaseReturnMilestone): WeightedLeaseReturnMilestoneView {
    return WeightedLeaseReturnMilestoneView(
        code = item.code,
        title = truncate(item.title, 64),
        channel = truncate(item.channel, 64),
        quantityText = item.quantity.toString(),
        approvedText = "${item.approved}",
        version = item.version.toString(),
        segmentText = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedLeaseReturnMilestoneViews(items: MutableList<WeightedLeaseReturnMilestone>): MutableList<WeightedLeaseReturnMilestoneView> {
    val views = mutableListOf<WeightedLeaseReturnMilestoneView>()
    for (item in items) {
        views.add(toWeightedLeaseReturnMilestoneView(item))
    }
    return views
}

fun sumWeightedLeaseReturnMilestoneView(view: WeightedLeaseReturnMilestoneView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedVersion = view.version.toInt()
    return parsedQuantity.toDouble() + parsedVersion.toDouble()
}
