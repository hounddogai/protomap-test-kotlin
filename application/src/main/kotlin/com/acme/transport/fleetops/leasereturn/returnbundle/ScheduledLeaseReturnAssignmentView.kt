package com.acme.transport.fleetops.leasereturn.returnbundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled lease return assignment. */
data class ScheduledLeaseReturnAssignmentView(
    var code: String,
    var name: String,
    var depot: String,
    var currencyText: String,
    var reviewer: String,
    var retries: String,
    var startsAt: String,
    var visibleText: String,
    var statusLabel: String,
)

fun toScheduledLeaseReturnAssignmentView(item: ScheduledLeaseReturnAssignment): ScheduledLeaseReturnAssignmentView {
    return ScheduledLeaseReturnAssignmentView(
        code = item.code,
        name = item.name,
        depot = item.depot,
        currencyText = item.currency,
        reviewer = truncate(item.reviewer, 40),
        retries = item.retries.toString(),
        startsAt = item.startsAt.toString(),
        visibleText = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledLeaseReturnAssignmentViews(items: MutableList<ScheduledLeaseReturnAssignment>): MutableList<ScheduledLeaseReturnAssignmentView> {
    val views = mutableListOf<ScheduledLeaseReturnAssignmentView>()
    for (item in items) {
        views.add(toScheduledLeaseReturnAssignmentView(item))
    }
    return views
}

fun sumScheduledLeaseReturnAssignmentView(view: ScheduledLeaseReturnAssignmentView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedRetries.toDouble() + parsedStartsAt.toDouble()
}
