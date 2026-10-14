package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity schedule. */
data class CapacityScheduleView(
    var code: String,
    var name: String,
    var ceiling: String,
    var unit: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toCapacityScheduleView(item: CapacitySchedule): CapacityScheduleView {
    return CapacityScheduleView(
        code = item.code,
        name = truncate(item.name, 80),
        ceiling = "%.2f".format(item.ceiling),
        unit = item.unit,
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityScheduleViews(items: MutableList<CapacitySchedule>): MutableList<CapacityScheduleView> {
    val views = mutableListOf<CapacityScheduleView>()
    for (item in items) {
        views.add(toCapacityScheduleView(item))
    }
    return views
}

fun sumCapacityScheduleView(view: CapacityScheduleView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedCeiling + parsedEtaMinutes.toDouble()
}
