package com.acme.transport.fleetops.inspection.estimate

/** How screens and exports show a inspection estimate. */
data class InspectionEstimateView(
    var code: String,
    var label: String,
    var minimum: String,
    var graceDays: String,
    var depot: String,
    var category: String,
    var timezone: String,
    var statusLabel: String,
)

fun toInspectionEstimateView(item: InspectionEstimate): InspectionEstimateView {
    return InspectionEstimateView(
        code = item.code,
        label = item.label,
        minimum = "%.2f".format(item.minimum),
        graceDays = item.graceDays.toString(),
        depot = item.depot,
        category = item.category,
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionEstimateViews(items: MutableList<InspectionEstimate>): MutableList<InspectionEstimateView> {
    val views = mutableListOf<InspectionEstimateView>()
    for (item in items) {
        views.add(toInspectionEstimateView(item))
    }
    return views
}

fun sumInspectionEstimateView(view: InspectionEstimateView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedMinimum + parsedGraceDays.toDouble()
}
