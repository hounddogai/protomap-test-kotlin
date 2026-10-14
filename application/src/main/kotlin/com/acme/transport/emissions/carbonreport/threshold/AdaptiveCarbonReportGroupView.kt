package com.acme.transport.emissions.carbonreport.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive carbon report group. */
data class AdaptiveCarbonReportGroupView(
    var code: String,
    var title: String,
    var fuelLitersText: String,
    var country: String,
    var retries: String,
    var carrier: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toAdaptiveCarbonReportGroupView(item: AdaptiveCarbonReportGroup): AdaptiveCarbonReportGroupView {
    return AdaptiveCarbonReportGroupView(
        code = item.code,
        title = truncate(item.title, 64),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        country = truncate(item.country, 80),
        retries = item.retries.toString(),
        carrier = item.carrier,
        segmentText = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveCarbonReportGroupViews(items: MutableList<AdaptiveCarbonReportGroup>): MutableList<AdaptiveCarbonReportGroupView> {
    val views = mutableListOf<AdaptiveCarbonReportGroupView>()
    for (item in items) {
        views.add(toAdaptiveCarbonReportGroupView(item))
    }
    return views
}

fun sumAdaptiveCarbonReportGroupView(view: AdaptiveCarbonReportGroupView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedFuelLiters + parsedRetries.toDouble()
}
