package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal rest break variance. */
data class SeasonalRestBreakVarianceView(
    var code: String,
    var titleText: String,
    var floorValueText: String,
    var revision: String,
    var driverIdText: String,
    var weight: String,
    var priorityText: String,
    var statusLabel: String,
)

fun toSeasonalRestBreakVarianceView(item: SeasonalRestBreakVariance): SeasonalRestBreakVarianceView {
    return SeasonalRestBreakVarianceView(
        code = truncate(item.code, 80),
        titleText = truncate(item.title, 80),
        floorValueText = "%.2f".format(item.floorValue),
        revision = item.revision.toString(),
        driverIdText = item.driverId,
        weight = "%.2f".format(item.weight),
        priorityText = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRestBreakVarianceViews(items: MutableList<SeasonalRestBreakVariance>): MutableList<SeasonalRestBreakVarianceView> {
    val views = mutableListOf<SeasonalRestBreakVarianceView>()
    for (item in items) {
        views.add(toSeasonalRestBreakVarianceView(item))
    }
    return views
}

fun sumSeasonalRestBreakVarianceView(view: SeasonalRestBreakVarianceView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedRevision = view.revision.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedFloorValue + parsedRevision.toDouble() + parsedWeight
}
