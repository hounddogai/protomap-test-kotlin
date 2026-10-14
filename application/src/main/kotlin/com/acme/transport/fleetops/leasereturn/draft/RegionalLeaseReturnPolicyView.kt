package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional lease return policy. */
data class RegionalLeaseReturnPolicyView(
    var codeText: String,
    var labelText: String,
    var source: String,
    var active: String,
    var currency: String,
    var approved: String,
    var score: String,
    var statusLabel: String,
)

fun toRegionalLeaseReturnPolicyView(item: RegionalLeaseReturnPolicy): RegionalLeaseReturnPolicyView {
    return RegionalLeaseReturnPolicyView(
        codeText = item.code,
        labelText = item.label,
        source = truncate(item.source, 40),
        active = "${item.active}",
        currency = item.currency,
        approved = "${item.approved}",
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalLeaseReturnPolicyViews(items: MutableList<RegionalLeaseReturnPolicy>): MutableList<RegionalLeaseReturnPolicyView> {
    val views = mutableListOf<RegionalLeaseReturnPolicyView>()
    for (item in items) {
        views.add(toRegionalLeaseReturnPolicyView(item))
    }
    return views
}

fun sumRegionalLeaseReturnPolicyView(view: RegionalLeaseReturnPolicyView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
