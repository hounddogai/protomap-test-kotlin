package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan window. */
data class LanePlanWindowView(
    var code: String,
    var name: String,
    var source: String,
    var lane: String,
    var graceDays: String,
    var activeText: String,
    var fuelLiters: String,
    var endsAtText: String,
    var odometer: String,
    var priority: String,
    var statusLabel: String,
)

fun toLanePlanWindowView(item: LanePlanWindow): LanePlanWindowView {
    return LanePlanWindowView(
        code = truncate(item.code, 80),
        name = item.name,
        source = item.source,
        lane = truncate(item.lane, 120),
        graceDays = item.graceDays.toString(),
        activeText = "${item.active}",
        fuelLiters = "%.2f".format(item.fuelLiters),
        endsAtText = item.endsAt.toString(),
        odometer = item.odometer.toString(),
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanWindowViews(items: MutableList<LanePlanWindow>): MutableList<LanePlanWindowView> {
    val views = mutableListOf<LanePlanWindowView>()
    for (item in items) {
        views.add(toLanePlanWindowView(item))
    }
    return views
}

fun sumLanePlanWindowView(view: LanePlanWindowView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedGraceDays.toDouble() + parsedFuelLiters + parsedEndsAt.toDouble()
}
