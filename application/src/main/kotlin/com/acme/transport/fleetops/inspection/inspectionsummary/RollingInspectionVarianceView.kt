package com.acme.transport.fleetops.inspection.inspectionsummary

/** How screens and exports show a rolling inspection variance. */
data class RollingInspectionVarianceView(
    var codeText: String,
    var nameText: String,
    var approved: String,
    var pinned: String,
    var label: String,
    var windowDays: String,
    var odometerText: String,
    var countryText: String,
    var margin: String,
    var statusLabel: String,
)

fun toRollingInspectionVarianceView(item: RollingInspectionVariance): RollingInspectionVarianceView {
    return RollingInspectionVarianceView(
        codeText = item.code,
        nameText = item.name,
        approved = "${item.approved}",
        pinned = "${item.pinned}",
        label = item.label,
        windowDays = item.windowDays.toString(),
        odometerText = item.odometer.toString(),
        countryText = item.country,
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingInspectionVarianceViews(items: MutableList<RollingInspectionVariance>): MutableList<RollingInspectionVarianceView> {
    val views = mutableListOf<RollingInspectionVarianceView>()
    for (item in items) {
        views.add(toRollingInspectionVarianceView(item))
    }
    return views
}

fun sumRollingInspectionVarianceView(view: RollingInspectionVarianceView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedOdometer = view.odometerText.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedWindowDays.toDouble() + parsedOdometer.toDouble() + parsedMargin
}
