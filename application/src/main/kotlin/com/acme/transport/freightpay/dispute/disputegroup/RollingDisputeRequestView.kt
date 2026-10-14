package com.acme.transport.freightpay.dispute.disputegroup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling dispute request. */
data class RollingDisputeRequestView(
    var code: String,
    var label: String,
    var description: String,
    var pallets: String,
    var dock: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toRollingDisputeRequestView(item: RollingDisputeRequest): RollingDisputeRequestView {
    return RollingDisputeRequestView(
        code = item.code,
        label = truncate(item.label, 120),
        description = item.description,
        pallets = item.pallets.toString(),
        dock = item.dock,
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDisputeRequestViews(items: MutableList<RollingDisputeRequest>): MutableList<RollingDisputeRequestView> {
    val views = mutableListOf<RollingDisputeRequestView>()
    for (item in items) {
        views.add(toRollingDisputeRequestView(item))
    }
    return views
}

fun sumRollingDisputeRequestView(view: RollingDisputeRequestView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedPallets.toDouble() + parsedUpdatedAt.toDouble()
}
