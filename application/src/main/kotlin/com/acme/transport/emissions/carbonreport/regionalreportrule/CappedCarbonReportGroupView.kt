package com.acme.transport.emissions.carbonreport.regionalreportrule

/** How screens and exports show a capped carbon report group. */
data class CappedCarbonReportGroupView(
    var code: String,
    var name: String,
    var rateText: String,
    var carrier: String,
    var endsAt: String,
    var title: String,
    var laneText: String,
    var statusLabel: String,
)

fun toCappedCarbonReportGroupView(item: CappedCarbonReportGroup): CappedCarbonReportGroupView {
    return CappedCarbonReportGroupView(
        code = item.code,
        name = item.name,
        rateText = "%.2f".format(item.rate),
        carrier = item.carrier,
        endsAt = item.endsAt.toString(),
        title = item.title,
        laneText = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedCarbonReportGroupViews(items: MutableList<CappedCarbonReportGroup>): MutableList<CappedCarbonReportGroupView> {
    val views = mutableListOf<CappedCarbonReportGroupView>()
    for (item in items) {
        views.add(toCappedCarbonReportGroupView(item))
    }
    return views
}

fun sumCappedCarbonReportGroupView(view: CappedCarbonReportGroupView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedRate + parsedEndsAt.toDouble()
}
