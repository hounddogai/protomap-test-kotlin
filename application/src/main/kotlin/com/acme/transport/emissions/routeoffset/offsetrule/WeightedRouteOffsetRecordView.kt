package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted route offset record. */
data class WeightedRouteOffsetRecordView(
    var code: String,
    var labelText: String,
    var graceDays: String,
    var version: String,
    var checksum: String,
    var priorityText: String,
    var unit: String,
    var channelText: String,
    var fuelLiters: String,
    var category: String,
    var statusLabel: String,
)

fun toWeightedRouteOffsetRecordView(item: WeightedRouteOffsetRecord): WeightedRouteOffsetRecordView {
    return WeightedRouteOffsetRecordView(
        code = item.code,
        labelText = item.label,
        graceDays = item.graceDays.toString(),
        version = item.version.toString(),
        checksum = item.checksum,
        priorityText = item.priority.toString(),
        unit = truncate(item.unit, 64),
        channelText = item.channel,
        fuelLiters = "%.2f".format(item.fuelLiters),
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedRouteOffsetRecordViews(items: MutableList<WeightedRouteOffsetRecord>): MutableList<WeightedRouteOffsetRecordView> {
    val views = mutableListOf<WeightedRouteOffsetRecordView>()
    for (item in items) {
        views.add(toWeightedRouteOffsetRecordView(item))
    }
    return views
}

fun sumWeightedRouteOffsetRecordView(view: WeightedRouteOffsetRecordView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedVersion = view.version.toInt()
    val parsedPriority = view.priorityText.toInt()
    return parsedGraceDays.toDouble() + parsedVersion.toDouble() + parsedPriority.toDouble()
}
