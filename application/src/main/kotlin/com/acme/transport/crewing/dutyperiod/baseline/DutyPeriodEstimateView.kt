package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period estimate. */
data class DutyPeriodEstimateView(
    var code: String,
    var label: String,
    var distanceKmText: String,
    var floorValueText: String,
    var threshold: String,
    var amount: String,
    var cost: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toDutyPeriodEstimateView(item: DutyPeriodEstimate): DutyPeriodEstimateView {
    return DutyPeriodEstimateView(
        code = item.code,
        label = truncate(item.label, 64),
        distanceKmText = "%.2f".format(item.distanceKm),
        floorValueText = "%.2f".format(item.floorValue),
        threshold = "%.2f".format(item.threshold),
        amount = "%.2f".format(item.amount),
        cost = "%.2f".format(item.cost),
        categoryText = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodEstimateViews(items: MutableList<DutyPeriodEstimate>): MutableList<DutyPeriodEstimateView> {
    val views = mutableListOf<DutyPeriodEstimateView>()
    for (item in items) {
        views.add(toDutyPeriodEstimateView(item))
    }
    return views
}

fun sumDutyPeriodEstimateView(view: DutyPeriodEstimateView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedDistanceKm + parsedFloorValue + parsedThreshold
}
