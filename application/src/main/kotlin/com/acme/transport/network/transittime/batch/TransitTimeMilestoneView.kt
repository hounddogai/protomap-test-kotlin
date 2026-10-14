package com.acme.transport.network.transittime.batch

/** How screens and exports show a transit time milestone. */
data class TransitTimeMilestoneView(
    var code: String,
    var label: String,
    var ceiling: String,
    var odometerText: String,
    var category: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toTransitTimeMilestoneView(item: TransitTimeMilestone): TransitTimeMilestoneView {
    return TransitTimeMilestoneView(
        code = item.code,
        label = item.label,
        ceiling = "%.2f".format(item.ceiling),
        odometerText = item.odometer.toString(),
        category = item.category,
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeMilestoneViews(items: MutableList<TransitTimeMilestone>): MutableList<TransitTimeMilestoneView> {
    val views = mutableListOf<TransitTimeMilestoneView>()
    for (item in items) {
        views.add(toTransitTimeMilestoneView(item))
    }
    return views
}

fun sumTransitTimeMilestoneView(view: TransitTimeMilestoneView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedCeiling + parsedOdometer.toDouble() + parsedDurationMinutes.toDouble()
}
