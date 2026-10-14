package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback carbon report group. */
data class FallbackCarbonReportGroupView(
    var code: String,
    var title: String,
    var discount: String,
    var windowDays: String,
    var floorValue: String,
    var category: String,
    var minimum: String,
    var graceDaysText: String,
    var limit: String,
    var country: String,
    var statusLabel: String,
)

fun toFallbackCarbonReportGroupView(item: FallbackCarbonReportGroup): FallbackCarbonReportGroupView {
    return FallbackCarbonReportGroupView(
        code = item.code,
        title = item.title,
        discount = "%.2f".format(item.discount),
        windowDays = item.windowDays.toString(),
        floorValue = "%.2f".format(item.floorValue),
        category = truncate(item.category, 64),
        minimum = "%.2f".format(item.minimum),
        graceDaysText = item.graceDays.toString(),
        limit = item.limit.toString(),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackCarbonReportGroupViews(items: MutableList<FallbackCarbonReportGroup>): MutableList<FallbackCarbonReportGroupView> {
    val views = mutableListOf<FallbackCarbonReportGroupView>()
    for (item in items) {
        views.add(toFallbackCarbonReportGroupView(item))
    }
    return views
}

fun sumFallbackCarbonReportGroupView(view: FallbackCarbonReportGroupView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedDiscount + parsedWindowDays.toDouble() + parsedFloorValue
}
