package com.acme.transport.customs.tariff.tariffdigest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive tariff metric. */
data class AdaptiveTariffMetricView(
    var code: String,
    var name: String,
    var limit: String,
    var stopCountText: String,
    var title: String,
    var quantity: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toAdaptiveTariffMetricView(item: AdaptiveTariffMetric): AdaptiveTariffMetricView {
    return AdaptiveTariffMetricView(
        code = item.code,
        name = truncate(item.name, 40),
        limit = item.limit.toString(),
        stopCountText = item.stopCount.toString(),
        title = item.title,
        quantity = item.quantity.toString(),
        routeCode = truncate(item.routeCode, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveTariffMetricViews(items: MutableList<AdaptiveTariffMetric>): MutableList<AdaptiveTariffMetricView> {
    val views = mutableListOf<AdaptiveTariffMetricView>()
    for (item in items) {
        views.add(toAdaptiveTariffMetricView(item))
    }
    return views
}

fun sumAdaptiveTariffMetricView(view: AdaptiveTariffMetricView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedLimit.toDouble() + parsedStopCount.toDouble() + parsedQuantity.toDouble()
}
