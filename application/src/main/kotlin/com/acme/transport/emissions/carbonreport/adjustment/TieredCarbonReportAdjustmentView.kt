package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered carbon report adjustment. */
data class TieredCarbonReportAdjustmentView(
    var code: String,
    var label: String,
    var rate: String,
    var odometer: String,
    var floorValueText: String,
    var dock: String,
    var category: String,
    var trailerId: String,
    var maximum: String,
    var approved: String,
    var statusLabel: String,
)

fun toTieredCarbonReportAdjustmentView(item: TieredCarbonReportAdjustment): TieredCarbonReportAdjustmentView {
    return TieredCarbonReportAdjustmentView(
        code = item.code,
        label = item.label,
        rate = "%.2f".format(item.rate),
        odometer = item.odometer.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        dock = item.dock,
        category = truncate(item.category, 40),
        trailerId = truncate(item.trailerId, 80),
        maximum = "%.2f".format(item.maximum),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredCarbonReportAdjustmentViews(items: MutableList<TieredCarbonReportAdjustment>): MutableList<TieredCarbonReportAdjustmentView> {
    val views = mutableListOf<TieredCarbonReportAdjustmentView>()
    for (item in items) {
        views.add(toTieredCarbonReportAdjustmentView(item))
    }
    return views
}

fun sumTieredCarbonReportAdjustmentView(view: TieredCarbonReportAdjustmentView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedOdometer = view.odometer.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedRate + parsedOdometer.toDouble() + parsedFloorValue
}
