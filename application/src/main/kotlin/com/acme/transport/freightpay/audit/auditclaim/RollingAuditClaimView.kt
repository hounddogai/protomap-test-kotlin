package com.acme.transport.freightpay.audit.auditclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling audit claim. */
data class RollingAuditClaimView(
    var code: String,
    var name: String,
    var lane: String,
    var phoneText: String,
    var endsAt: String,
    var loadKgText: String,
    var budgetText: String,
    var carrierText: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toRollingAuditClaimView(item: RollingAuditClaim): RollingAuditClaimView {
    return RollingAuditClaimView(
        code = item.code,
        name = truncate(item.name, 120),
        lane = item.lane,
        phoneText = item.phone,
        endsAt = item.endsAt.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        budgetText = "%.2f".format(item.budget),
        carrierText = item.carrier,
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingAuditClaimViews(items: MutableList<RollingAuditClaim>): MutableList<RollingAuditClaimView> {
    val views = mutableListOf<RollingAuditClaimView>()
    for (item in items) {
        views.add(toRollingAuditClaimView(item))
    }
    return views
}

fun sumRollingAuditClaimView(view: RollingAuditClaimView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedBudget = view.budgetText.toDouble()
    return parsedEndsAt.toDouble() + parsedLoadKg + parsedBudget
}
