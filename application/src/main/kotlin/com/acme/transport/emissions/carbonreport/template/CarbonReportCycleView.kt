package com.acme.transport.emissions.carbonreport.template

/** How screens and exports show a carbon report cycle. */
data class CarbonReportCycleView(
    var code: String,
    var label: String,
    var currencyText: String,
    var driverId: String,
    var depot: String,
    var amountText: String,
    var statusLabel: String,
)

fun toCarbonReportCycleView(item: CarbonReportCycle): CarbonReportCycleView {
    return CarbonReportCycleView(
        code = item.code,
        label = item.label,
        currencyText = item.currency,
        driverId = item.driverId,
        depot = item.depot,
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCarbonReportCycleViews(items: MutableList<CarbonReportCycle>): MutableList<CarbonReportCycleView> {
    val views = mutableListOf<CarbonReportCycleView>()
    for (item in items) {
        views.add(toCarbonReportCycleView(item))
    }
    return views
}

fun sumCarbonReportCycleView(view: CarbonReportCycleView): Double {
    val parsedAmount = view.amountText.toDouble()
    return parsedAmount
}
