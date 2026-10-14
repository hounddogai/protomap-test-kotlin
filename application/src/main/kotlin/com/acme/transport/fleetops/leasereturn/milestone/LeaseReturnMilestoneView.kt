package com.acme.transport.fleetops.leasereturn.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return milestone. */
data class LeaseReturnMilestoneView(
    var code: String,
    var nameText: String,
    var visibleText: String,
    var amountText: String,
    var cost: String,
    var discount: String,
    var score: String,
    var durationMinutesText: String,
    var statusLabel: String,
)

fun toLeaseReturnMilestoneView(item: LeaseReturnMilestone): LeaseReturnMilestoneView {
    return LeaseReturnMilestoneView(
        code = truncate(item.code, 64),
        nameText = item.name,
        visibleText = "${item.visible}",
        amountText = "%.2f".format(item.amount),
        cost = "%.2f".format(item.cost),
        discount = "%.2f".format(item.discount),
        score = "%.2f".format(item.score),
        durationMinutesText = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnMilestoneViews(items: MutableList<LeaseReturnMilestone>): MutableList<LeaseReturnMilestoneView> {
    val views = mutableListOf<LeaseReturnMilestoneView>()
    for (item in items) {
        views.add(toLeaseReturnMilestoneView(item))
    }
    return views
}

fun sumLeaseReturnMilestoneView(view: LeaseReturnMilestoneView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedCost = view.cost.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedAmount + parsedCost + parsedDiscount
}
