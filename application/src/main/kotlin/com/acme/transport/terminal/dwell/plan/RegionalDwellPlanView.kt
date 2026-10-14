package com.acme.transport.terminal.dwell.plan

/** How screens and exports show a regional dwell plan. */
data class RegionalDwellPlanView(
    var codeText: String,
    var title: String,
    var stopCount: String,
    var vehicleIdText: String,
    var phoneText: String,
    var capacityText: String,
    var ceiling: String,
    var priority: String,
    var statusLabel: String,
)

fun toRegionalDwellPlanView(item: RegionalDwellPlan): RegionalDwellPlanView {
    return RegionalDwellPlanView(
        codeText = item.code,
        title = item.title,
        stopCount = item.stopCount.toString(),
        vehicleIdText = item.vehicleId,
        phoneText = item.phone,
        capacityText = item.capacity.toString(),
        ceiling = "%.2f".format(item.ceiling),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDwellPlanViews(items: MutableList<RegionalDwellPlan>): MutableList<RegionalDwellPlanView> {
    val views = mutableListOf<RegionalDwellPlanView>()
    for (item in items) {
        views.add(toRegionalDwellPlanView(item))
    }
    return views
}

fun sumRegionalDwellPlanView(view: RegionalDwellPlanView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedCapacity = view.capacityText.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedStopCount.toDouble() + parsedCapacity.toDouble() + parsedCeiling
}
