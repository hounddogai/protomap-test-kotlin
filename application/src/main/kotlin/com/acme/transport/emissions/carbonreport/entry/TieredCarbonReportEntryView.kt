package com.acme.transport.emissions.carbonreport.entry

/** How screens and exports show a tiered carbon report entry. */
data class TieredCarbonReportEntryView(
    var code: String,
    var title: String,
    var batchSize: String,
    var windowDays: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toTieredCarbonReportEntryView(item: TieredCarbonReportEntry): TieredCarbonReportEntryView {
    return TieredCarbonReportEntryView(
        code = item.code,
        title = item.title,
        batchSize = item.batchSize.toString(),
        windowDays = item.windowDays.toString(),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredCarbonReportEntryViews(items: MutableList<TieredCarbonReportEntry>): MutableList<TieredCarbonReportEntryView> {
    val views = mutableListOf<TieredCarbonReportEntryView>()
    for (item in items) {
        views.add(toTieredCarbonReportEntryView(item))
    }
    return views
}

fun sumTieredCarbonReportEntryView(view: TieredCarbonReportEntryView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedBatchSize.toDouble() + parsedWindowDays.toDouble()
}
