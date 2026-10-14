package com.acme.transport.freightpay.dispute.disputegroup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling dispute group. */
data class RollingDisputeGroupView(
    var code: String,
    var title: String,
    var externalRef: String,
    var trailerIdText: String,
    var expiresAtText: String,
    var statusLabel: String,
)

fun toRollingDisputeGroupView(item: RollingDisputeGroup): RollingDisputeGroupView {
    return RollingDisputeGroupView(
        code = truncate(item.code, 64),
        title = item.title,
        externalRef = item.externalRef,
        trailerIdText = item.trailerId,
        expiresAtText = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDisputeGroupViews(items: MutableList<RollingDisputeGroup>): MutableList<RollingDisputeGroupView> {
    val views = mutableListOf<RollingDisputeGroupView>()
    for (item in items) {
        views.add(toRollingDisputeGroupView(item))
    }
    return views
}

fun sumRollingDisputeGroupView(view: RollingDisputeGroupView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedExpiresAt.toDouble()
}
