package com.acme.transport.lastmile.locker.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a locker schedule. */
data class LockerScheduleView(
    var code: String,
    var title: String,
    var currency: String,
    var maximumText: String,
    var segment: String,
    var fuelLiters: String,
    var category: String,
    var margin: String,
    var statusLabel: String,
)

fun toLockerScheduleView(item: LockerSchedule): LockerScheduleView {
    return LockerScheduleView(
        code = item.code,
        title = truncate(item.title, 40),
        currency = item.currency,
        maximumText = "%.2f".format(item.maximum),
        segment = truncate(item.segment, 120),
        fuelLiters = "%.2f".format(item.fuelLiters),
        category = item.category,
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerScheduleViews(items: MutableList<LockerSchedule>): MutableList<LockerScheduleView> {
    val views = mutableListOf<LockerScheduleView>()
    for (item in items) {
        views.add(toLockerScheduleView(item))
    }
    return views
}

fun sumLockerScheduleView(view: LockerScheduleView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedMaximum + parsedFuelLiters + parsedMargin
}
