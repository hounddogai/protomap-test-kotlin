package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default carbon report mapping. */
data class DefaultCarbonReportMappingView(
    var code: String,
    var titleText: String,
    var startsAt: String,
    var archived: String,
    var retries: String,
    var segmentText: String,
    var summary: String,
    var statusLabel: String,
)

fun toDefaultCarbonReportMappingView(item: DefaultCarbonReportMapping): DefaultCarbonReportMappingView {
    return DefaultCarbonReportMappingView(
        code = item.code,
        titleText = item.title,
        startsAt = item.startsAt.toString(),
        archived = "${item.archived}",
        retries = item.retries.toString(),
        segmentText = item.segment,
        summary = truncate(item.summary, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultCarbonReportMappingViews(items: MutableList<DefaultCarbonReportMapping>): MutableList<DefaultCarbonReportMappingView> {
    val views = mutableListOf<DefaultCarbonReportMappingView>()
    for (item in items) {
        views.add(toDefaultCarbonReportMappingView(item))
    }
    return views
}

fun sumDefaultCarbonReportMappingView(view: DefaultCarbonReportMappingView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedStartsAt.toDouble() + parsedRetries.toDouble()
}
