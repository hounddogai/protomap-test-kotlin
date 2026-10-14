package com.acme.transport.freightpay.audit.legacyauditpolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary audit policy. */
data class PrimaryAuditPolicyView(
    var code: String,
    var titleText: String,
    var approved: String,
    var updatedAtText: String,
    var odometer: String,
    var description: String,
    var priority: String,
    var version: String,
    var statusLabel: String,
)

fun toPrimaryAuditPolicyView(item: PrimaryAuditPolicy): PrimaryAuditPolicyView {
    return PrimaryAuditPolicyView(
        code = truncate(item.code, 40),
        titleText = item.title,
        approved = "${item.approved}",
        updatedAtText = item.updatedAt.toString(),
        odometer = item.odometer.toString(),
        description = item.description,
        priority = item.priority.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryAuditPolicyViews(items: MutableList<PrimaryAuditPolicy>): MutableList<PrimaryAuditPolicyView> {
    val views = mutableListOf<PrimaryAuditPolicyView>()
    for (item in items) {
        views.add(toPrimaryAuditPolicyView(item))
    }
    return views
}

fun sumPrimaryAuditPolicyView(view: PrimaryAuditPolicyView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedUpdatedAt.toDouble() + parsedOdometer.toDouble() + parsedPriority.toDouble()
}
