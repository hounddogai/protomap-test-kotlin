package com.acme.transport.fleetops.tire.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire metric. */
data class TireMetricView(
    var code: String,
    var title: String,
    var margin: String,
    var locale: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toTireMetricView(item: TireMetric): TireMetricView {
    return TireMetricView(
        code = truncate(item.code, 40),
        title = item.title,
        margin = "%.2f".format(item.margin),
        locale = item.locale,
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireMetricViews(items: MutableList<TireMetric>): MutableList<TireMetricView> {
    val views = mutableListOf<TireMetricView>()
    for (item in items) {
        views.add(toTireMetricView(item))
    }
    return views
}

fun sumTireMetricView(view: TireMetricView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedMargin + parsedExpiresAt.toDouble()
}
