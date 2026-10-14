package com.acme.transport.crewing.restbreak.breakpreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional rest break rule. */
data class RegionalRestBreakRuleView(
    var code: String,
    var label: String,
    var trailerId: String,
    var ownerId: String,
    var durationMinutes: String,
    var priority: String,
    var statusLabel: String,
)

fun toRegionalRestBreakRuleView(item: RegionalRestBreakRule): RegionalRestBreakRuleView {
    return RegionalRestBreakRuleView(
        code = item.code,
        label = item.label,
        trailerId = truncate(item.trailerId, 120),
        ownerId = truncate(item.ownerId, 80),
        durationMinutes = item.durationMinutes.toString(),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalRestBreakRuleViews(items: MutableList<RegionalRestBreakRule>): MutableList<RegionalRestBreakRuleView> {
    val views = mutableListOf<RegionalRestBreakRuleView>()
    for (item in items) {
        views.add(toRegionalRestBreakRuleView(item))
    }
    return views
}

fun sumRegionalRestBreakRuleView(view: RegionalRestBreakRuleView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedDurationMinutes.toDouble() + parsedPriority.toDouble()
}
