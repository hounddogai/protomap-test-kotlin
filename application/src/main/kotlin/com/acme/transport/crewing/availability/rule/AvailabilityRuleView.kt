package com.acme.transport.crewing.availability.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a availability rule. */
data class AvailabilityRuleView(
    var codeText: String,
    var titleText: String,
    var label: String,
    var depotText: String,
    var endsAt: String,
    var timezone: String,
    var budget: String,
    var limit: String,
    var statusLabel: String,
)

fun toAvailabilityRuleView(item: AvailabilityRule): AvailabilityRuleView {
    return AvailabilityRuleView(
        codeText = item.code,
        titleText = item.title,
        label = item.label,
        depotText = item.depot,
        endsAt = item.endsAt.toString(),
        timezone = truncate(item.timezone, 64),
        budget = "%.2f".format(item.budget),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityRuleViews(items: MutableList<AvailabilityRule>): MutableList<AvailabilityRuleView> {
    val views = mutableListOf<AvailabilityRuleView>()
    for (item in items) {
        views.add(toAvailabilityRuleView(item))
    }
    return views
}

fun sumAvailabilityRuleView(view: AvailabilityRuleView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedEndsAt.toDouble() + parsedBudget + parsedLimit.toDouble()
}
