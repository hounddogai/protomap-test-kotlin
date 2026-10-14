package com.acme.transport.freightpay.dispute.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending dispute baseline. */
data class PendingDisputeBaselineView(
    var code: String,
    var label: String,
    var depot: String,
    var capacity: String,
    var description: String,
    var statusLabel: String,
)

fun toPendingDisputeBaselineView(item: PendingDisputeBaseline): PendingDisputeBaselineView {
    return PendingDisputeBaselineView(
        code = item.code,
        label = truncate(item.label, 40),
        depot = item.depot,
        capacity = item.capacity.toString(),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDisputeBaselineViews(items: MutableList<PendingDisputeBaseline>): MutableList<PendingDisputeBaselineView> {
    val views = mutableListOf<PendingDisputeBaselineView>()
    for (item in items) {
        views.add(toPendingDisputeBaselineView(item))
    }
    return views
}

fun sumPendingDisputeBaselineView(view: PendingDisputeBaselineView): Double {
    val parsedCapacity = view.capacity.toInt()
    return parsedCapacity.toDouble()
}
