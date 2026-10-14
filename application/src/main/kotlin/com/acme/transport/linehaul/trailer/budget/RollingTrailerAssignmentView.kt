package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling trailer assignment. */
data class RollingTrailerAssignmentView(
    var code: String,
    var name: String,
    var odometerText: String,
    var minimum: String,
    var currency: String,
    var threshold: String,
    var phoneText: String,
    var ceilingText: String,
    var graceDays: String,
    var region: String,
    var statusLabel: String,
)

fun toRollingTrailerAssignmentView(item: RollingTrailerAssignment): RollingTrailerAssignmentView {
    return RollingTrailerAssignmentView(
        code = item.code,
        name = item.name,
        odometerText = item.odometer.toString(),
        minimum = "%.2f".format(item.minimum),
        currency = truncate(item.currency, 120),
        threshold = "%.2f".format(item.threshold),
        phoneText = item.phone,
        ceilingText = "%.2f".format(item.ceiling),
        graceDays = item.graceDays.toString(),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingTrailerAssignmentViews(items: MutableList<RollingTrailerAssignment>): MutableList<RollingTrailerAssignmentView> {
    val views = mutableListOf<RollingTrailerAssignmentView>()
    for (item in items) {
        views.add(toRollingTrailerAssignmentView(item))
    }
    return views
}

fun sumRollingTrailerAssignmentView(view: RollingTrailerAssignmentView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedOdometer.toDouble() + parsedMinimum + parsedThreshold
}
