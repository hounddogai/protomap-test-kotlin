package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break metric. */
data class RestBreakMetricView(
    var code: String,
    var nameText: String,
    var timezone: String,
    var trailerId: String,
    var fuelLiters: String,
    var region: String,
    var createdAt: String,
    var externalRefText: String,
    var rate: String,
    var statusLabel: String,
)

fun toRestBreakMetricView(item: RestBreakMetric): RestBreakMetricView {
    return RestBreakMetricView(
        code = item.code,
        nameText = truncate(item.name, 80),
        timezone = truncate(item.timezone, 80),
        trailerId = item.trailerId,
        fuelLiters = "%.2f".format(item.fuelLiters),
        region = item.region,
        createdAt = item.createdAt.toString(),
        externalRefText = truncate(item.externalRef, 64),
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakMetricViews(items: MutableList<RestBreakMetric>): MutableList<RestBreakMetricView> {
    val views = mutableListOf<RestBreakMetricView>()
    for (item in items) {
        views.add(toRestBreakMetricView(item))
    }
    return views
}

fun sumRestBreakMetricView(view: RestBreakMetricView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedFuelLiters + parsedCreatedAt.toDouble() + parsedRate
}
