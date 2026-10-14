package com.acme.transport.linehaul.trailer.cycle

/** How screens and exports show a manual trailer metric. */
data class ManualTrailerMetricView(
    var code: String,
    var name: String,
    var graceDaysText: String,
    var carrier: String,
    var timezone: String,
    var reviewer: String,
    var depot: String,
    var statusLabel: String,
)

fun toManualTrailerMetricView(item: ManualTrailerMetric): ManualTrailerMetricView {
    return ManualTrailerMetricView(
        code = item.code,
        name = item.name,
        graceDaysText = item.graceDays.toString(),
        carrier = item.carrier,
        timezone = item.timezone,
        reviewer = item.reviewer,
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTrailerMetricViews(items: MutableList<ManualTrailerMetric>): MutableList<ManualTrailerMetricView> {
    val views = mutableListOf<ManualTrailerMetricView>()
    for (item in items) {
        views.add(toManualTrailerMetricView(item))
    }
    return views
}

fun sumManualTrailerMetricView(view: ManualTrailerMetricView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedGraceDays.toDouble()
}
