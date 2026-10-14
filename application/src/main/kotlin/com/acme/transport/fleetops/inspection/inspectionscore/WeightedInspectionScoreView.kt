package com.acme.transport.fleetops.inspection.inspectionscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted inspection score. */
data class WeightedInspectionScoreView(
    var code: String,
    var labelText: String,
    var activeText: String,
    var lane: String,
    var batchSize: String,
    var durationMinutes: String,
    var version: String,
    var statusLabel: String,
)

fun toWeightedInspectionScoreView(item: WeightedInspectionScore): WeightedInspectionScoreView {
    return WeightedInspectionScoreView(
        code = truncate(item.code, 40),
        labelText = item.label,
        activeText = "${item.active}",
        lane = item.lane,
        batchSize = item.batchSize.toString(),
        durationMinutes = item.durationMinutes.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedInspectionScoreViews(items: MutableList<WeightedInspectionScore>): MutableList<WeightedInspectionScoreView> {
    val views = mutableListOf<WeightedInspectionScoreView>()
    for (item in items) {
        views.add(toWeightedInspectionScoreView(item))
    }
    return views
}

fun sumWeightedInspectionScoreView(view: WeightedInspectionScoreView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedVersion = view.version.toInt()
    return parsedBatchSize.toDouble() + parsedDurationMinutes.toDouble() + parsedVersion.toDouble()
}
