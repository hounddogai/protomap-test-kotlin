package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered qualification metric. */
data class TieredQualificationMetricView(
    var code: String,
    var title: String,
    var margin: String,
    var sourceText: String,
    var windowDays: String,
    var threshold: String,
    var channel: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toTieredQualificationMetricView(item: TieredQualificationMetric): TieredQualificationMetricView {
    return TieredQualificationMetricView(
        code = truncate(item.code, 120),
        title = item.title,
        margin = "%.2f".format(item.margin),
        sourceText = truncate(item.source, 80),
        windowDays = item.windowDays.toString(),
        threshold = "%.2f".format(item.threshold),
        channel = item.channel,
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredQualificationMetricViews(items: MutableList<TieredQualificationMetric>): MutableList<TieredQualificationMetricView> {
    val views = mutableListOf<TieredQualificationMetricView>()
    for (item in items) {
        views.add(toTieredQualificationMetricView(item))
    }
    return views
}

fun sumTieredQualificationMetricView(view: TieredQualificationMetricView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedMargin + parsedWindowDays.toDouble() + parsedThreshold
}
