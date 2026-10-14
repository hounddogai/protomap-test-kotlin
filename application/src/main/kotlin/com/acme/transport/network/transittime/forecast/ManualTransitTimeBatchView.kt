package com.acme.transport.network.transittime.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual transit time batch. */
data class ManualTransitTimeBatchView(
    var codeText: String,
    var titleText: String,
    var cost: String,
    var category: String,
    var maximum: String,
    var timezone: String,
    var version: String,
    var pinned: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toManualTransitTimeBatchView(item: ManualTransitTimeBatch): ManualTransitTimeBatchView {
    return ManualTransitTimeBatchView(
        codeText = item.code,
        titleText = item.title,
        cost = "%.2f".format(item.cost),
        category = item.category,
        maximum = "%.2f".format(item.maximum),
        timezone = truncate(item.timezone, 80),
        version = item.version.toString(),
        pinned = "${item.pinned}",
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTransitTimeBatchViews(items: MutableList<ManualTransitTimeBatch>): MutableList<ManualTransitTimeBatchView> {
    val views = mutableListOf<ManualTransitTimeBatchView>()
    for (item in items) {
        views.add(toManualTransitTimeBatchView(item))
    }
    return views
}

fun sumManualTransitTimeBatchView(view: ManualTransitTimeBatchView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedCost + parsedMaximum + parsedVersion.toDouble()
}
