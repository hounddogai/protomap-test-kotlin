package com.acme.transport.network.node.nodescore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional node score. */
data class RegionalNodeScoreView(
    var code: String,
    var name: String,
    var batchSize: String,
    var active: String,
    var timezone: String,
    var notes: String,
    var approved: String,
    var floorValue: String,
    var depot: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toRegionalNodeScoreView(item: RegionalNodeScore): RegionalNodeScoreView {
    return RegionalNodeScoreView(
        code = item.code,
        name = item.name,
        batchSize = item.batchSize.toString(),
        active = "${item.active}",
        timezone = truncate(item.timezone, 40),
        notes = item.notes,
        approved = "${item.approved}",
        floorValue = "%.2f".format(item.floorValue),
        depot = item.depot,
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalNodeScoreViews(items: MutableList<RegionalNodeScore>): MutableList<RegionalNodeScoreView> {
    val views = mutableListOf<RegionalNodeScoreView>()
    for (item in items) {
        views.add(toRegionalNodeScoreView(item))
    }
    return views
}

fun sumRegionalNodeScoreView(view: RegionalNodeScoreView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedBatchSize.toDouble() + parsedFloorValue + parsedWindowDays.toDouble()
}
