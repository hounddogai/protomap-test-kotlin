package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer yard group. */
data class TrailerYardGroupView(
    var code: String,
    var name: String,
    var maximum: String,
    var graceDays: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toTrailerYardGroupView(item: TrailerYardGroup): TrailerYardGroupView {
    return TrailerYardGroupView(
        code = truncate(item.code, 80),
        name = item.name,
        maximum = "%.2f".format(item.maximum),
        graceDays = item.graceDays.toString(),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardGroupViews(items: MutableList<TrailerYardGroup>): MutableList<TrailerYardGroupView> {
    val views = mutableListOf<TrailerYardGroupView>()
    for (item in items) {
        views.add(toTrailerYardGroupView(item))
    }
    return views
}

fun sumTrailerYardGroupView(view: TrailerYardGroupView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedMaximum + parsedGraceDays.toDouble()
}
