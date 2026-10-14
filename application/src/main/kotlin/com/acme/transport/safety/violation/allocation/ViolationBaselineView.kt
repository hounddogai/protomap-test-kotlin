package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation baseline. */
data class ViolationBaselineView(
    var code: String,
    var label: String,
    var etaMinutes: String,
    var name: String,
    var revision: String,
    var budgetText: String,
    var costText: String,
    var channelText: String,
    var timezone: String,
    var statusLabel: String,
)

fun toViolationBaselineView(item: ViolationBaseline): ViolationBaselineView {
    return ViolationBaselineView(
        code = item.code,
        label = truncate(item.label, 120),
        etaMinutes = item.etaMinutes.toString(),
        name = item.name,
        revision = item.revision.toString(),
        budgetText = "%.2f".format(item.budget),
        costText = "%.2f".format(item.cost),
        channelText = item.channel,
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationBaselineViews(items: MutableList<ViolationBaseline>): MutableList<ViolationBaselineView> {
    val views = mutableListOf<ViolationBaselineView>()
    for (item in items) {
        views.add(toViolationBaselineView(item))
    }
    return views
}

fun sumViolationBaselineView(view: ViolationBaselineView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedRevision.toDouble() + parsedBudget
}
