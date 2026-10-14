package com.acme.transport.freightpay.audit.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict audit rule. */
data class StrictAuditRuleView(
    var code: String,
    var label: String,
    var distanceKm: String,
    var contactEmail: String,
    var priorityText: String,
    var carrier: String,
    var budget: String,
    var channel: String,
    var maximum: String,
    var statusLabel: String,
)

fun toStrictAuditRuleView(item: StrictAuditRule): StrictAuditRuleView {
    return StrictAuditRuleView(
        code = item.code,
        label = truncate(item.label, 80),
        distanceKm = "%.2f".format(item.distanceKm),
        contactEmail = item.contactEmail,
        priorityText = item.priority.toString(),
        carrier = truncate(item.carrier, 120),
        budget = "%.2f".format(item.budget),
        channel = truncate(item.channel, 40),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictAuditRuleViews(items: MutableList<StrictAuditRule>): MutableList<StrictAuditRuleView> {
    val views = mutableListOf<StrictAuditRuleView>()
    for (item in items) {
        views.add(toStrictAuditRuleView(item))
    }
    return views
}

fun sumStrictAuditRuleView(view: StrictAuditRuleView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedPriority = view.priorityText.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedDistanceKm + parsedPriority.toDouble() + parsedBudget
}
