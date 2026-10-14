package com.acme.transport.linehaul.laneplan.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan threshold. */
data class LanePlanThresholdView(
    var code: String,
    var title: String,
    var routeCode: String,
    var durationMinutesText: String,
    var capacity: String,
    var odometer: String,
    var active: String,
    var locale: String,
    var revision: String,
    var rate: String,
    var statusLabel: String,
)

fun toLanePlanThresholdView(item: LanePlanThreshold): LanePlanThresholdView {
    return LanePlanThresholdView(
        code = item.code,
        title = item.title,
        routeCode = item.routeCode,
        durationMinutesText = item.durationMinutes.toString(),
        capacity = item.capacity.toString(),
        odometer = item.odometer.toString(),
        active = "${item.active}",
        locale = truncate(item.locale, 120),
        revision = item.revision.toString(),
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanThresholdViews(items: MutableList<LanePlanThreshold>): MutableList<LanePlanThresholdView> {
    val views = mutableListOf<LanePlanThresholdView>()
    for (item in items) {
        views.add(toLanePlanThresholdView(item))
    }
    return views
}

fun sumLanePlanThresholdView(view: LanePlanThresholdView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedDurationMinutes.toDouble() + parsedCapacity.toDouble() + parsedOdometer.toDouble()
}
