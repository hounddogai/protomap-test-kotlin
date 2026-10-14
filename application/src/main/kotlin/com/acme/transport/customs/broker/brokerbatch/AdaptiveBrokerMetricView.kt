package com.acme.transport.customs.broker.brokerbatch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive broker metric. */
data class AdaptiveBrokerMetricView(
    var code: String,
    var name: String,
    var lane: String,
    var summaryText: String,
    var labelText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toAdaptiveBrokerMetricView(item: AdaptiveBrokerMetric): AdaptiveBrokerMetricView {
    return AdaptiveBrokerMetricView(
        code = item.code,
        name = truncate(item.name, 40),
        lane = truncate(item.lane, 40),
        summaryText = item.summary,
        labelText = item.label,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveBrokerMetricViews(items: MutableList<AdaptiveBrokerMetric>): MutableList<AdaptiveBrokerMetricView> {
    val views = mutableListOf<AdaptiveBrokerMetricView>()
    for (item in items) {
        views.add(toAdaptiveBrokerMetricView(item))
    }
    return views
}

fun sumAdaptiveBrokerMetricView(view: AdaptiveBrokerMetricView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
