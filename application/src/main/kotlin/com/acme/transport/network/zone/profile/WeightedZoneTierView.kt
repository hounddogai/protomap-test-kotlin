package com.acme.transport.network.zone.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted zone tier. */
data class WeightedZoneTierView(
    var codeText: String,
    var labelText: String,
    var capacity: String,
    var routeCodeText: String,
    var shiftCodeText: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toWeightedZoneTierView(item: WeightedZoneTier): WeightedZoneTierView {
    return WeightedZoneTierView(
        codeText = truncate(item.code, 120),
        labelText = truncate(item.label, 64),
        capacity = item.capacity.toString(),
        routeCodeText = item.routeCode,
        shiftCodeText = truncate(item.shiftCode, 80),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedZoneTierViews(items: MutableList<WeightedZoneTier>): MutableList<WeightedZoneTierView> {
    val views = mutableListOf<WeightedZoneTierView>()
    for (item in items) {
        views.add(toWeightedZoneTierView(item))
    }
    return views
}

fun sumWeightedZoneTierView(view: WeightedZoneTierView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedCapacity.toDouble() + parsedStopCount.toDouble()
}
