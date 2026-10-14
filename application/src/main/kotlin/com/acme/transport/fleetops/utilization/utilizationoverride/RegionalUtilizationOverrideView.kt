package com.acme.transport.fleetops.utilization.utilizationoverride

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional utilization override. */
data class RegionalUtilizationOverrideView(
    var code: String,
    var nameText: String,
    var source: String,
    var retries: String,
    var priority: String,
    var pinned: String,
    var budget: String,
    var durationMinutes: String,
    var title: String,
    var statusLabel: String,
)

fun toRegionalUtilizationOverrideView(item: RegionalUtilizationOverride): RegionalUtilizationOverrideView {
    return RegionalUtilizationOverrideView(
        code = item.code,
        nameText = truncate(item.name, 64),
        source = item.source,
        retries = item.retries.toString(),
        priority = item.priority.toString(),
        pinned = "${item.pinned}",
        budget = "%.2f".format(item.budget),
        durationMinutes = item.durationMinutes.toString(),
        title = truncate(item.title, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalUtilizationOverrideViews(items: MutableList<RegionalUtilizationOverride>): MutableList<RegionalUtilizationOverrideView> {
    val views = mutableListOf<RegionalUtilizationOverrideView>()
    for (item in items) {
        views.add(toRegionalUtilizationOverrideView(item))
    }
    return views
}

fun sumRegionalUtilizationOverrideView(view: RegionalUtilizationOverrideView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedRetries.toDouble() + parsedPriority.toDouble() + parsedBudget
}
