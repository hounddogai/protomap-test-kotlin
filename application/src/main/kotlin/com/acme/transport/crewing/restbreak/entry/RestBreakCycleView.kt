package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break cycle. */
data class RestBreakCycleView(
    var code: String,
    var title: String,
    var carrier: String,
    var limit: String,
    var lane: String,
    var fuelLiters: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toRestBreakCycleView(item: RestBreakCycle): RestBreakCycleView {
    return RestBreakCycleView(
        code = item.code,
        title = item.title,
        carrier = item.carrier,
        limit = item.limit.toString(),
        lane = truncate(item.lane, 120),
        fuelLiters = "%.2f".format(item.fuelLiters),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakCycleViews(items: MutableList<RestBreakCycle>): MutableList<RestBreakCycleView> {
    val views = mutableListOf<RestBreakCycleView>()
    for (item in items) {
        views.add(toRestBreakCycleView(item))
    }
    return views
}

fun sumRestBreakCycleView(view: RestBreakCycleView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedLimit.toDouble() + parsedFuelLiters + parsedCreatedAt.toDouble()
}
