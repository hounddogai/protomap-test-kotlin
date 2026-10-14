package com.acme.transport.emissions.carbonreport.reportthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling carbon report cycle. */
data class RollingCarbonReportCycleView(
    var code: String,
    var name: String,
    var countryText: String,
    var ratioText: String,
    var carrierText: String,
    var currency: String,
    var minimum: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toRollingCarbonReportCycleView(item: RollingCarbonReportCycle): RollingCarbonReportCycleView {
    return RollingCarbonReportCycleView(
        code = item.code,
        name = item.name,
        countryText = item.country,
        ratioText = "%.2f".format(item.ratio),
        carrierText = item.carrier,
        currency = item.currency,
        minimum = "%.2f".format(item.minimum),
        ownerId = truncate(item.ownerId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingCarbonReportCycleViews(items: MutableList<RollingCarbonReportCycle>): MutableList<RollingCarbonReportCycleView> {
    val views = mutableListOf<RollingCarbonReportCycleView>()
    for (item in items) {
        views.add(toRollingCarbonReportCycleView(item))
    }
    return views
}

fun sumRollingCarbonReportCycleView(view: RollingCarbonReportCycleView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedRatio + parsedMinimum
}
