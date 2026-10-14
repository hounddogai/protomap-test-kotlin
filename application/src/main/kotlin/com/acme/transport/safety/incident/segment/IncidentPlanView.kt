package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident plan. */
data class IncidentPlanView(
    var codeText: String,
    var label: String,
    var vehicleId: String,
    var costText: String,
    var ownerId: String,
    var trailerIdText: String,
    var country: String,
    var windowDays: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toIncidentPlanView(item: IncidentPlan): IncidentPlanView {
    return IncidentPlanView(
        codeText = truncate(item.code, 64),
        label = item.label,
        vehicleId = item.vehicleId,
        costText = "%.2f".format(item.cost),
        ownerId = truncate(item.ownerId, 80),
        trailerIdText = item.trailerId,
        country = truncate(item.country, 64),
        windowDays = item.windowDays.toString(),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentPlanViews(items: MutableList<IncidentPlan>): MutableList<IncidentPlanView> {
    val views = mutableListOf<IncidentPlanView>()
    for (item in items) {
        views.add(toIncidentPlanView(item))
    }
    return views
}

fun sumIncidentPlanView(view: IncidentPlanView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedCost + parsedWindowDays.toDouble() + parsedStartsAt.toDouble()
}
