package com.acme.transport.fleetops.leasereturn.returnpreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending lease return preference. */
data class PendingLeaseReturnPreferenceView(
    var code: String,
    var title: String,
    var depot: String,
    var budget: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toPendingLeaseReturnPreferenceView(item: PendingLeaseReturnPreference): PendingLeaseReturnPreferenceView {
    return PendingLeaseReturnPreferenceView(
        code = truncate(item.code, 120),
        title = item.title,
        depot = item.depot,
        budget = "%.2f".format(item.budget),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingLeaseReturnPreferenceViews(items: MutableList<PendingLeaseReturnPreference>): MutableList<PendingLeaseReturnPreferenceView> {
    val views = mutableListOf<PendingLeaseReturnPreferenceView>()
    for (item in items) {
        views.add(toPendingLeaseReturnPreferenceView(item))
    }
    return views
}

fun sumPendingLeaseReturnPreferenceView(view: PendingLeaseReturnPreferenceView): Double {
    val parsedBudget = view.budget.toDouble()
    return parsedBudget
}
