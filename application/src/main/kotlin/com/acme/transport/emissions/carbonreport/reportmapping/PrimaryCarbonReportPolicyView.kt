package com.acme.transport.emissions.carbonreport.reportmapping

/** How screens and exports show a primary carbon report policy. */
data class PrimaryCarbonReportPolicyView(
    var code: String,
    var nameText: String,
    var label: String,
    var retries: String,
    var weight: String,
    var odometer: String,
    var windowDaysText: String,
    var scoreText: String,
    var unit: String,
    var depotText: String,
    var category: String,
    var statusLabel: String,
)

fun toPrimaryCarbonReportPolicyView(item: PrimaryCarbonReportPolicy): PrimaryCarbonReportPolicyView {
    return PrimaryCarbonReportPolicyView(
        code = item.code,
        nameText = item.name,
        label = item.label,
        retries = item.retries.toString(),
        weight = "%.2f".format(item.weight),
        odometer = item.odometer.toString(),
        windowDaysText = item.windowDays.toString(),
        scoreText = "%.2f".format(item.score),
        unit = item.unit,
        depotText = item.depot,
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCarbonReportPolicyViews(items: MutableList<PrimaryCarbonReportPolicy>): MutableList<PrimaryCarbonReportPolicyView> {
    val views = mutableListOf<PrimaryCarbonReportPolicyView>()
    for (item in items) {
        views.add(toPrimaryCarbonReportPolicyView(item))
    }
    return views
}

fun sumPrimaryCarbonReportPolicyView(view: PrimaryCarbonReportPolicyView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedRetries.toDouble() + parsedWeight + parsedOdometer.toDouble()
}
