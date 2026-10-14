package com.acme.transport.crewing.availability.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling availability mapping. */
data class RollingAvailabilityMappingView(
    var code: String,
    var name: String,
    var retries: String,
    var costText: String,
    var driverIdText: String,
    var routeCode: String,
    var reviewer: String,
    var priorityText: String,
    var statusLabel: String,
)

fun toRollingAvailabilityMappingView(item: RollingAvailabilityMapping): RollingAvailabilityMappingView {
    return RollingAvailabilityMappingView(
        code = item.code,
        name = item.name,
        retries = item.retries.toString(),
        costText = "%.2f".format(item.cost),
        driverIdText = item.driverId,
        routeCode = truncate(item.routeCode, 80),
        reviewer = item.reviewer,
        priorityText = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingAvailabilityMappingViews(items: MutableList<RollingAvailabilityMapping>): MutableList<RollingAvailabilityMappingView> {
    val views = mutableListOf<RollingAvailabilityMappingView>()
    for (item in items) {
        views.add(toRollingAvailabilityMappingView(item))
    }
    return views
}

fun sumRollingAvailabilityMappingView(view: RollingAvailabilityMappingView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedCost = view.costText.toDouble()
    val parsedPriority = view.priorityText.toInt()
    return parsedRetries.toDouble() + parsedCost + parsedPriority.toDouble()
}
