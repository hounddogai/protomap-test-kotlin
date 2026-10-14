package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled fuel burn cycle. */
data class ScheduledFuelBurnCycleView(
    var codeText: String,
    var nameText: String,
    var category: String,
    var label: String,
    var durationMinutes: String,
    var timezone: String,
    var costText: String,
    var routeCode: String,
    var active: String,
    var priority: String,
    var statusLabel: String,
)

fun toScheduledFuelBurnCycleView(item: ScheduledFuelBurnCycle): ScheduledFuelBurnCycleView {
    return ScheduledFuelBurnCycleView(
        codeText = truncate(item.code, 64),
        nameText = truncate(item.name, 64),
        category = item.category,
        label = item.label,
        durationMinutes = item.durationMinutes.toString(),
        timezone = item.timezone,
        costText = "%.2f".format(item.cost),
        routeCode = truncate(item.routeCode, 80),
        active = "${item.active}",
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledFuelBurnCycleViews(items: MutableList<ScheduledFuelBurnCycle>): MutableList<ScheduledFuelBurnCycleView> {
    val views = mutableListOf<ScheduledFuelBurnCycleView>()
    for (item in items) {
        views.add(toScheduledFuelBurnCycleView(item))
    }
    return views
}

fun sumScheduledFuelBurnCycleView(view: ScheduledFuelBurnCycleView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedCost = view.costText.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedDurationMinutes.toDouble() + parsedCost + parsedPriority.toDouble()
}
