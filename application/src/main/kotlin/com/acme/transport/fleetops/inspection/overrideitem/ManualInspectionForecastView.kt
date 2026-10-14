package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual inspection forecast. */
data class ManualInspectionForecastView(
    var code: String,
    var title: String,
    var updatedAt: String,
    var thresholdText: String,
    var description: String,
    var discountText: String,
    var lane: String,
    var palletsText: String,
    var trailerId: String,
    var reviewer: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toManualInspectionForecastView(item: ManualInspectionForecast): ManualInspectionForecastView {
    return ManualInspectionForecastView(
        code = item.code,
        title = item.title,
        updatedAt = item.updatedAt.toString(),
        thresholdText = "%.2f".format(item.threshold),
        description = truncate(item.description, 40),
        discountText = "%.2f".format(item.discount),
        lane = item.lane,
        palletsText = item.pallets.toString(),
        trailerId = item.trailerId,
        reviewer = truncate(item.reviewer, 80),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualInspectionForecastViews(items: MutableList<ManualInspectionForecast>): MutableList<ManualInspectionForecastView> {
    val views = mutableListOf<ManualInspectionForecastView>()
    for (item in items) {
        views.add(toManualInspectionForecastView(item))
    }
    return views
}

fun sumManualInspectionForecastView(view: ManualInspectionForecastView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    return parsedUpdatedAt.toDouble() + parsedThreshold + parsedDiscount
}
