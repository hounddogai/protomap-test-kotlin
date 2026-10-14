package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict near miss group. */
data class StrictNearMissGroupView(
    var code: String,
    var name: String,
    var summary: String,
    var regionText: String,
    var odometer: String,
    var dock: String,
    var costText: String,
    var channel: String,
    var statusLabel: String,
)

fun toStrictNearMissGroupView(item: StrictNearMissGroup): StrictNearMissGroupView {
    return StrictNearMissGroupView(
        code = truncate(item.code, 64),
        name = item.name,
        summary = item.summary,
        regionText = item.region,
        odometer = item.odometer.toString(),
        dock = item.dock,
        costText = "%.2f".format(item.cost),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictNearMissGroupViews(items: MutableList<StrictNearMissGroup>): MutableList<StrictNearMissGroupView> {
    val views = mutableListOf<StrictNearMissGroupView>()
    for (item in items) {
        views.add(toStrictNearMissGroupView(item))
    }
    return views
}

fun sumStrictNearMissGroupView(view: StrictNearMissGroupView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedOdometer.toDouble() + parsedCost
}
