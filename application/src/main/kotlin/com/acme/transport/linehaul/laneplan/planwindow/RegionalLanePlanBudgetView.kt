package com.acme.transport.linehaul.laneplan.planwindow

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional lane plan budget. */
data class RegionalLanePlanBudgetView(
    var code: String,
    var label: String,
    var revision: String,
    var durationMinutesText: String,
    var updatedAtText: String,
    var summary: String,
    var marginText: String,
    var statusLabel: String,
)

fun toRegionalLanePlanBudgetView(item: RegionalLanePlanBudget): RegionalLanePlanBudgetView {
    return RegionalLanePlanBudgetView(
        code = item.code,
        label = truncate(item.label, 80),
        revision = item.revision.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        updatedAtText = item.updatedAt.toString(),
        summary = item.summary,
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalLanePlanBudgetViews(items: MutableList<RegionalLanePlanBudget>): MutableList<RegionalLanePlanBudgetView> {
    val views = mutableListOf<RegionalLanePlanBudgetView>()
    for (item in items) {
        views.add(toRegionalLanePlanBudgetView(item))
    }
    return views
}

fun sumRegionalLanePlanBudgetView(view: RegionalLanePlanBudgetView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedRevision.toDouble() + parsedDurationMinutes.toDouble() + parsedUpdatedAt.toDouble()
}
