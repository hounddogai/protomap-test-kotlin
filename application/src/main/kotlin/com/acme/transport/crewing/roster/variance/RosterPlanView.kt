package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster plan. */
data class RosterPlanView(
    var codeText: String,
    var title: String,
    var labelText: String,
    var priorityText: String,
    var timezoneText: String,
    var fuelLiters: String,
    var source: String,
    var statusLabel: String,
)

fun toRosterPlanView(item: RosterPlan): RosterPlanView {
    return RosterPlanView(
        codeText = truncate(item.code, 120),
        title = item.title,
        labelText = truncate(item.label, 80),
        priorityText = item.priority.toString(),
        timezoneText = truncate(item.timezone, 80),
        fuelLiters = "%.2f".format(item.fuelLiters),
        source = truncate(item.source, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterPlanViews(items: MutableList<RosterPlan>): MutableList<RosterPlanView> {
    val views = mutableListOf<RosterPlanView>()
    for (item in items) {
        views.add(toRosterPlanView(item))
    }
    return views
}

fun sumRosterPlanView(view: RosterPlanView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedPriority.toDouble() + parsedFuelLiters
}
