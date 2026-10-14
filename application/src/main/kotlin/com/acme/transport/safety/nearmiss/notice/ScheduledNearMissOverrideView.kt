package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled near miss override. */
data class ScheduledNearMissOverrideView(
    var code: String,
    var name: String,
    var category: String,
    var weight: String,
    var fuelLiters: String,
    var unit: String,
    var statusLabel: String,
)

fun toScheduledNearMissOverrideView(item: ScheduledNearMissOverride): ScheduledNearMissOverrideView {
    return ScheduledNearMissOverrideView(
        code = truncate(item.code, 120),
        name = truncate(item.name, 120),
        category = item.category,
        weight = "%.2f".format(item.weight),
        fuelLiters = "%.2f".format(item.fuelLiters),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledNearMissOverrideViews(items: MutableList<ScheduledNearMissOverride>): MutableList<ScheduledNearMissOverrideView> {
    val views = mutableListOf<ScheduledNearMissOverrideView>()
    for (item in items) {
        views.add(toScheduledNearMissOverrideView(item))
    }
    return views
}

fun sumScheduledNearMissOverrideView(view: ScheduledNearMissOverrideView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedWeight + parsedFuelLiters
}
