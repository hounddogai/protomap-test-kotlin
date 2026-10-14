package com.acme.transport.safety.incident.incidentmanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident budget. */
data class IncidentBudgetView(
    var code: String,
    var name: String,
    var active: String,
    var lane: String,
    var capacity: String,
    var reviewer: String,
    var threshold: String,
    var statusLabel: String,
)

fun toIncidentBudgetView(item: IncidentBudget): IncidentBudgetView {
    return IncidentBudgetView(
        code = item.code,
        name = truncate(item.name, 80),
        active = "${item.active}",
        lane = item.lane,
        capacity = item.capacity.toString(),
        reviewer = item.reviewer,
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentBudgetViews(items: MutableList<IncidentBudget>): MutableList<IncidentBudgetView> {
    val views = mutableListOf<IncidentBudgetView>()
    for (item in items) {
        views.add(toIncidentBudgetView(item))
    }
    return views
}

fun sumIncidentBudgetView(view: IncidentBudgetView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedCapacity.toDouble() + parsedThreshold
}
