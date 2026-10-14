package com.acme.transport.emissions.routeoffset.offsetassignment

/** How screens and exports show a route offset budget. */
data class RouteOffsetBudgetView(
    var codeText: String,
    var label: String,
    var floorValueText: String,
    var sequence: String,
    var depotText: String,
    var odometerText: String,
    var timezoneText: String,
    var capacity: String,
    var country: String,
    var retries: String,
    var statusLabel: String,
)

fun toRouteOffsetBudgetView(item: RouteOffsetBudget): RouteOffsetBudgetView {
    return RouteOffsetBudgetView(
        codeText = item.code,
        label = item.label,
        floorValueText = "%.2f".format(item.floorValue),
        sequence = item.sequence.toString(),
        depotText = item.depot,
        odometerText = item.odometer.toString(),
        timezoneText = item.timezone,
        capacity = item.capacity.toString(),
        country = item.country,
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetBudgetViews(items: MutableList<RouteOffsetBudget>): MutableList<RouteOffsetBudgetView> {
    val views = mutableListOf<RouteOffsetBudgetView>()
    for (item in items) {
        views.add(toRouteOffsetBudgetView(item))
    }
    return views
}

fun sumRouteOffsetBudgetView(view: RouteOffsetBudgetView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedOdometer = view.odometerText.toInt()
    return parsedFloorValue + parsedSequence.toDouble() + parsedOdometer.toDouble()
}
