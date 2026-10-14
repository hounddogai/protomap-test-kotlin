package com.acme.transport.emissions.carbonreport.adaptivereportrule

/** How screens and exports show a strict carbon report batch. */
data class StrictCarbonReportBatchView(
    var code: String,
    var titleText: String,
    var pallets: String,
    var channel: String,
    var dock: String,
    var nameText: String,
    var statusLabel: String,
)

fun toStrictCarbonReportBatchView(item: StrictCarbonReportBatch): StrictCarbonReportBatchView {
    return StrictCarbonReportBatchView(
        code = item.code,
        titleText = item.title,
        pallets = item.pallets.toString(),
        channel = item.channel,
        dock = item.dock,
        nameText = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictCarbonReportBatchViews(items: MutableList<StrictCarbonReportBatch>): MutableList<StrictCarbonReportBatchView> {
    val views = mutableListOf<StrictCarbonReportBatchView>()
    for (item in items) {
        views.add(toStrictCarbonReportBatchView(item))
    }
    return views
}

fun sumStrictCarbonReportBatchView(view: StrictCarbonReportBatchView): Double {
    val parsedPallets = view.pallets.toInt()
    return parsedPallets.toDouble()
}
