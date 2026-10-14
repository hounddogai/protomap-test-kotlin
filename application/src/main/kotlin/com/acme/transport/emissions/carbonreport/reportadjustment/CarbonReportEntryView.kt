package com.acme.transport.emissions.carbonreport.reportadjustment

/** How screens and exports show a carbon report entry. */
data class CarbonReportEntryView(
    var code: String,
    var labelText: String,
    var batchSize: String,
    var dock: String,
    var routeCodeText: String,
    var activeText: String,
    var statusLabel: String,
)

fun toCarbonReportEntryView(item: CarbonReportEntry): CarbonReportEntryView {
    return CarbonReportEntryView(
        code = item.code,
        labelText = item.label,
        batchSize = item.batchSize.toString(),
        dock = item.dock,
        routeCodeText = item.routeCode,
        activeText = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportEntryViews(items: MutableList<CarbonReportEntry>): MutableList<CarbonReportEntryView> {
    val views = mutableListOf<CarbonReportEntryView>()
    for (item in items) {
        views.add(toCarbonReportEntryView(item))
    }
    return views
}

fun sumCarbonReportEntryView(view: CarbonReportEntryView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    return parsedBatchSize.toDouble()
}
