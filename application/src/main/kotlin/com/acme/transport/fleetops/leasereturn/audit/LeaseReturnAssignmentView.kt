package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return assignment. */
data class LeaseReturnAssignmentView(
    var code: String,
    var label: String,
    var limitText: String,
    var dock: String,
    var currency: String,
    var createdAt: String,
    var depot: String,
    var cost: String,
    var pallets: String,
    var carrier: String,
    var statusLabel: String,
)

fun toLeaseReturnAssignmentView(item: LeaseReturnAssignment): LeaseReturnAssignmentView {
    return LeaseReturnAssignmentView(
        code = item.code,
        label = item.label,
        limitText = item.limit.toString(),
        dock = truncate(item.dock, 80),
        currency = item.currency,
        createdAt = item.createdAt.toString(),
        depot = item.depot,
        cost = "%.2f".format(item.cost),
        pallets = item.pallets.toString(),
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnAssignmentViews(items: MutableList<LeaseReturnAssignment>): MutableList<LeaseReturnAssignmentView> {
    val views = mutableListOf<LeaseReturnAssignmentView>()
    for (item in items) {
        views.add(toLeaseReturnAssignmentView(item))
    }
    return views
}

fun sumLeaseReturnAssignmentView(view: LeaseReturnAssignmentView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedLimit.toDouble() + parsedCreatedAt.toDouble() + parsedCost
}
