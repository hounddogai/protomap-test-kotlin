package com.acme.transport.terminal.sortation.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation metric. */
data class SortationMetricView(
    var code: String,
    var nameText: String,
    var routeCodeText: String,
    var odometerText: String,
    var archived: String,
    var unitText: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toSortationMetricView(item: SortationMetric): SortationMetricView {
    return SortationMetricView(
        code = item.code,
        nameText = truncate(item.name, 40),
        routeCodeText = truncate(item.routeCode, 120),
        odometerText = item.odometer.toString(),
        archived = "${item.archived}",
        unitText = item.unit,
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationMetricViews(items: MutableList<SortationMetric>): MutableList<SortationMetricView> {
    val views = mutableListOf<SortationMetricView>()
    for (item in items) {
        views.add(toSortationMetricView(item))
    }
    return views
}

fun sumSortationMetricView(view: SortationMetricView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedOdometer.toDouble() + parsedBatchSize.toDouble()
}
