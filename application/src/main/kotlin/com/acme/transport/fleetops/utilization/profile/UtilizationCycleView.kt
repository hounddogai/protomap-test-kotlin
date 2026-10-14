package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a utilization cycle. */
data class UtilizationCycleView(
    var code: String,
    var labelText: String,
    var fuelLiters: String,
    var pinned: String,
    var title: String,
    var externalRefText: String,
    var priority: String,
    var statusLabel: String,
)

fun toUtilizationCycleView(item: UtilizationCycle): UtilizationCycleView {
    return UtilizationCycleView(
        code = truncate(item.code, 40),
        labelText = item.label,
        fuelLiters = "%.2f".format(item.fuelLiters),
        pinned = "${item.pinned}",
        title = item.title,
        externalRefText = truncate(item.externalRef, 40),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toUtilizationCycleViews(items: MutableList<UtilizationCycle>): MutableList<UtilizationCycleView> {
    val views = mutableListOf<UtilizationCycleView>()
    for (item in items) {
        views.add(toUtilizationCycleView(item))
    }
    return views
}

fun sumUtilizationCycleView(view: UtilizationCycleView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedFuelLiters + parsedPriority.toDouble()
}
