package com.acme.transport.freightpay.audit.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling audit policy. */
data class RollingAuditPolicyView(
    var code: String,
    var label: String,
    var pinnedText: String,
    var cost: String,
    var expiresAt: String,
    var reviewer: String,
    var approved: String,
    var statusLabel: String,
)

fun toRollingAuditPolicyView(item: RollingAuditPolicy): RollingAuditPolicyView {
    return RollingAuditPolicyView(
        code = item.code,
        label = truncate(item.label, 120),
        pinnedText = "${item.pinned}",
        cost = "%.2f".format(item.cost),
        expiresAt = item.expiresAt.toString(),
        reviewer = truncate(item.reviewer, 80),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingAuditPolicyViews(items: MutableList<RollingAuditPolicy>): MutableList<RollingAuditPolicyView> {
    val views = mutableListOf<RollingAuditPolicyView>()
    for (item in items) {
        views.add(toRollingAuditPolicyView(item))
    }
    return views
}

fun sumRollingAuditPolicyView(view: RollingAuditPolicyView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedCost + parsedExpiresAt.toDouble()
}
