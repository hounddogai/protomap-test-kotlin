package com.acme.transport.network.transittime.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict transit time score. */
data class StrictTransitTimeScoreView(
    var code: String,
    var nameText: String,
    var retries: String,
    var ceiling: String,
    var carrier: String,
    var routeCodeText: String,
    var floorValue: String,
    var depot: String,
    var sequenceText: String,
    var maximumText: String,
    var statusLabel: String,
)

fun toStrictTransitTimeScoreView(item: StrictTransitTimeScore): StrictTransitTimeScoreView {
    return StrictTransitTimeScoreView(
        code = truncate(item.code, 120),
        nameText = item.name,
        retries = item.retries.toString(),
        ceiling = "%.2f".format(item.ceiling),
        carrier = truncate(item.carrier, 64),
        routeCodeText = item.routeCode,
        floorValue = "%.2f".format(item.floorValue),
        depot = truncate(item.depot, 64),
        sequenceText = item.sequence.toString(),
        maximumText = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictTransitTimeScoreViews(items: MutableList<StrictTransitTimeScore>): MutableList<StrictTransitTimeScoreView> {
    val views = mutableListOf<StrictTransitTimeScoreView>()
    for (item in items) {
        views.add(toStrictTransitTimeScoreView(item))
    }
    return views
}

fun sumStrictTransitTimeScoreView(view: StrictTransitTimeScoreView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedRetries.toDouble() + parsedCeiling + parsedFloorValue
}
