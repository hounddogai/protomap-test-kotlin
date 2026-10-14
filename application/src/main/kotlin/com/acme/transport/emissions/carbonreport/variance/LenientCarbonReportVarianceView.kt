package com.acme.transport.emissions.carbonreport.variance

/** How screens and exports show a lenient carbon report variance. */
data class LenientCarbonReportVarianceView(
    var code: String,
    var labelText: String,
    var capacity: String,
    var etaMinutes: String,
    var distanceKmText: String,
    var segment: String,
    var statusLabel: String,
)

fun toLenientCarbonReportVarianceView(item: LenientCarbonReportVariance): LenientCarbonReportVarianceView {
    return LenientCarbonReportVarianceView(
        code = item.code,
        labelText = item.label,
        capacity = item.capacity.toString(),
        etaMinutes = item.etaMinutes.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientCarbonReportVarianceViews(items: MutableList<LenientCarbonReportVariance>): MutableList<LenientCarbonReportVarianceView> {
    val views = mutableListOf<LenientCarbonReportVarianceView>()
    for (item in items) {
        views.add(toLenientCarbonReportVarianceView(item))
    }
    return views
}

fun sumLenientCarbonReportVarianceView(view: LenientCarbonReportVarianceView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedCapacity.toDouble() + parsedEtaMinutes.toDouble() + parsedDistanceKm
}
