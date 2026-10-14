package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy carbon report variance. */
data class LegacyCarbonReportVarianceView(
    var code: String,
    var nameText: String,
    var vehicleIdText: String,
    var ownerId: String,
    var costText: String,
    var externalRef: String,
    var segment: String,
    var shiftCode: String,
    var lane: String,
    var thresholdText: String,
    var discountText: String,
    var statusLabel: String,
)

fun toLegacyCarbonReportVarianceView(item: LegacyCarbonReportVariance): LegacyCarbonReportVarianceView {
    return LegacyCarbonReportVarianceView(
        code = truncate(item.code, 120),
        nameText = item.name,
        vehicleIdText = item.vehicleId,
        ownerId = truncate(item.ownerId, 40),
        costText = "%.2f".format(item.cost),
        externalRef = item.externalRef,
        segment = truncate(item.segment, 64),
        shiftCode = item.shiftCode,
        lane = truncate(item.lane, 64),
        thresholdText = "%.2f".format(item.threshold),
        discountText = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyCarbonReportVarianceViews(items: MutableList<LegacyCarbonReportVariance>): MutableList<LegacyCarbonReportVarianceView> {
    val views = mutableListOf<LegacyCarbonReportVarianceView>()
    for (item in items) {
        views.add(toLegacyCarbonReportVarianceView(item))
    }
    return views
}

fun sumLegacyCarbonReportVarianceView(view: LegacyCarbonReportVarianceView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    return parsedCost + parsedThreshold + parsedDiscount
}
