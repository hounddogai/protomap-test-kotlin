package com.acme.transport.network.transittime.timethreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal transit time metric. */
data class SeasonalTransitTimeMetricView(
    var codeText: String,
    var title: String,
    var amount: String,
    var endsAt: String,
    var weight: String,
    var floorValueText: String,
    var locale: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toSeasonalTransitTimeMetricView(item: SeasonalTransitTimeMetric): SeasonalTransitTimeMetricView {
    return SeasonalTransitTimeMetricView(
        codeText = truncate(item.code, 80),
        title = item.title,
        amount = "%.2f".format(item.amount),
        endsAt = item.endsAt.toString(),
        weight = "%.2f".format(item.weight),
        floorValueText = "%.2f".format(item.floorValue),
        locale = item.locale,
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalTransitTimeMetricViews(items: MutableList<SeasonalTransitTimeMetric>): MutableList<SeasonalTransitTimeMetricView> {
    val views = mutableListOf<SeasonalTransitTimeMetricView>()
    for (item in items) {
        views.add(toSeasonalTransitTimeMetricView(item))
    }
    return views
}

fun sumSeasonalTransitTimeMetricView(view: SeasonalTransitTimeMetricView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedAmount + parsedEndsAt.toDouble() + parsedWeight
}
