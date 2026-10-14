package com.acme.transport.emissions.carbonreport.reservation

/** How screens and exports show a fallback carbon report variance. */
data class FallbackCarbonReportVarianceView(
    var code: String,
    var labelText: String,
    var name: String,
    var approved: String,
    var startsAtText: String,
    var segment: String,
    var graceDaysText: String,
    var retries: String,
    var timezone: String,
    var unit: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toFallbackCarbonReportVarianceView(item: FallbackCarbonReportVariance): FallbackCarbonReportVarianceView {
    return FallbackCarbonReportVarianceView(
        code = item.code,
        labelText = item.label,
        name = item.name,
        approved = "${item.approved}",
        startsAtText = item.startsAt.toString(),
        segment = item.segment,
        graceDaysText = item.graceDays.toString(),
        retries = item.retries.toString(),
        timezone = item.timezone,
        unit = item.unit,
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackCarbonReportVarianceViews(items: MutableList<FallbackCarbonReportVariance>): MutableList<FallbackCarbonReportVarianceView> {
    val views = mutableListOf<FallbackCarbonReportVarianceView>()
    for (item in items) {
        views.add(toFallbackCarbonReportVarianceView(item))
    }
    return views
}

fun sumFallbackCarbonReportVarianceView(view: FallbackCarbonReportVarianceView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedStartsAt.toDouble() + parsedGraceDays.toDouble() + parsedRetries.toDouble()
}
