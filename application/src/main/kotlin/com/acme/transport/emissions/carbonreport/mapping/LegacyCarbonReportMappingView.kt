package com.acme.transport.emissions.carbonreport.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy carbon report mapping. */
data class LegacyCarbonReportMappingView(
    var code: String,
    var title: String,
    var ceiling: String,
    var driverId: String,
    var summary: String,
    var dockText: String,
    var ratio: String,
    var capacity: String,
    var channel: String,
    var statusLabel: String,
)

fun toLegacyCarbonReportMappingView(item: LegacyCarbonReportMapping): LegacyCarbonReportMappingView {
    return LegacyCarbonReportMappingView(
        code = item.code,
        title = item.title,
        ceiling = "%.2f".format(item.ceiling),
        driverId = truncate(item.driverId, 64),
        summary = item.summary,
        dockText = truncate(item.dock, 64),
        ratio = "%.2f".format(item.ratio),
        capacity = item.capacity.toString(),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyCarbonReportMappingViews(items: MutableList<LegacyCarbonReportMapping>): MutableList<LegacyCarbonReportMappingView> {
    val views = mutableListOf<LegacyCarbonReportMappingView>()
    for (item in items) {
        views.add(toLegacyCarbonReportMappingView(item))
    }
    return views
}

fun sumLegacyCarbonReportMappingView(view: LegacyCarbonReportMappingView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedRatio = view.ratio.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedCeiling + parsedRatio + parsedCapacity.toDouble()
}
