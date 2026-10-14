package com.acme.transport.fleetops.leasereturn.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return segment. */
data class LeaseReturnSegmentView(
    var code: String,
    var label: String,
    var approved: String,
    var budget: String,
    var visible: String,
    var amount: String,
    var locale: String,
    var versionText: String,
    var statusLabel: String,
)

fun toLeaseReturnSegmentView(item: LeaseReturnSegment): LeaseReturnSegmentView {
    return LeaseReturnSegmentView(
        code = item.code,
        label = item.label,
        approved = "${item.approved}",
        budget = "%.2f".format(item.budget),
        visible = "${item.visible}",
        amount = "%.2f".format(item.amount),
        locale = truncate(item.locale, 64),
        versionText = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnSegmentViews(items: MutableList<LeaseReturnSegment>): MutableList<LeaseReturnSegmentView> {
    val views = mutableListOf<LeaseReturnSegmentView>()
    for (item in items) {
        views.add(toLeaseReturnSegmentView(item))
    }
    return views
}

fun sumLeaseReturnSegmentView(view: LeaseReturnSegmentView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedAmount = view.amount.toDouble()
    val parsedVersion = view.versionText.toInt()
    return parsedBudget + parsedAmount + parsedVersion.toDouble()
}
