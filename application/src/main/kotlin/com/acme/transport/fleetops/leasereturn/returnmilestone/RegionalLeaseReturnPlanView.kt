package com.acme.transport.fleetops.leasereturn.returnmilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional lease return plan. */
data class RegionalLeaseReturnPlanView(
    var codeText: String,
    var titleText: String,
    var category: String,
    var attempts: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toRegionalLeaseReturnPlanView(item: RegionalLeaseReturnPlan): RegionalLeaseReturnPlanView {
    return RegionalLeaseReturnPlanView(
        codeText = truncate(item.code, 40),
        titleText = item.title,
        category = item.category,
        attempts = item.attempts.toString(),
        currencyText = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalLeaseReturnPlanViews(items: MutableList<RegionalLeaseReturnPlan>): MutableList<RegionalLeaseReturnPlanView> {
    val views = mutableListOf<RegionalLeaseReturnPlanView>()
    for (item in items) {
        views.add(toRegionalLeaseReturnPlanView(item))
    }
    return views
}

fun sumRegionalLeaseReturnPlanView(view: RegionalLeaseReturnPlanView): Double {
    val parsedAttempts = view.attempts.toInt()
    return parsedAttempts.toDouble()
}
