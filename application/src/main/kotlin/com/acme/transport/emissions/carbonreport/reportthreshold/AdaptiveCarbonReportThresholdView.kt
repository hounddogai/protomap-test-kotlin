package com.acme.transport.emissions.carbonreport.reportthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive carbon report threshold. */
data class AdaptiveCarbonReportThresholdView(
    var code: String,
    var title: String,
    var timezoneText: String,
    var visible: String,
    var routeCodeText: String,
    var limit: String,
    var budget: String,
    var quantityText: String,
    var endsAt: String,
    var margin: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toAdaptiveCarbonReportThresholdView(item: AdaptiveCarbonReportThreshold): AdaptiveCarbonReportThresholdView {
    return AdaptiveCarbonReportThresholdView(
        code = item.code,
        title = truncate(item.title, 80),
        timezoneText = item.timezone,
        visible = "${item.visible}",
        routeCodeText = truncate(item.routeCode, 40),
        limit = item.limit.toString(),
        budget = "%.2f".format(item.budget),
        quantityText = item.quantity.toString(),
        endsAt = item.endsAt.toString(),
        margin = "%.2f".format(item.margin),
        phoneText = truncate(item.phone, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveCarbonReportThresholdViews(items: MutableList<AdaptiveCarbonReportThreshold>): MutableList<AdaptiveCarbonReportThresholdView> {
    val views = mutableListOf<AdaptiveCarbonReportThresholdView>()
    for (item in items) {
        views.add(toAdaptiveCarbonReportThresholdView(item))
    }
    return views
}

fun sumAdaptiveCarbonReportThresholdView(view: AdaptiveCarbonReportThresholdView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    return parsedLimit.toDouble() + parsedBudget + parsedQuantity.toDouble()
}
