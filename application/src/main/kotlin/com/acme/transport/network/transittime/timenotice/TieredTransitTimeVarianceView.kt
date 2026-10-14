package com.acme.transport.network.transittime.timenotice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered transit time variance. */
data class TieredTransitTimeVarianceView(
    var code: String,
    var nameText: String,
    var stopCount: String,
    var laneText: String,
    var minimumText: String,
    var shiftCode: String,
    var budget: String,
    var durationMinutes: String,
    var source: String,
    var priority: String,
    var statusLabel: String,
)

fun toTieredTransitTimeVarianceView(item: TieredTransitTimeVariance): TieredTransitTimeVarianceView {
    return TieredTransitTimeVarianceView(
        code = item.code,
        nameText = item.name,
        stopCount = item.stopCount.toString(),
        laneText = truncate(item.lane, 80),
        minimumText = "%.2f".format(item.minimum),
        shiftCode = truncate(item.shiftCode, 80),
        budget = "%.2f".format(item.budget),
        durationMinutes = item.durationMinutes.toString(),
        source = item.source,
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredTransitTimeVarianceViews(items: MutableList<TieredTransitTimeVariance>): MutableList<TieredTransitTimeVarianceView> {
    val views = mutableListOf<TieredTransitTimeVarianceView>()
    for (item in items) {
        views.add(toTieredTransitTimeVarianceView(item))
    }
    return views
}

fun sumTieredTransitTimeVarianceView(view: TieredTransitTimeVarianceView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedStopCount.toDouble() + parsedMinimum + parsedBudget
}
