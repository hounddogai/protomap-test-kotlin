package com.acme.transport.terminal.sortation.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered sortation metric. */
data class TieredSortationMetricView(
    var code: String,
    var title: String,
    var source: String,
    var region: String,
    var ratio: String,
    var statusLabel: String,
)

fun toTieredSortationMetricView(item: TieredSortationMetric): TieredSortationMetricView {
    return TieredSortationMetricView(
        code = item.code,
        title = truncate(item.title, 64),
        source = item.source,
        region = item.region,
        ratio = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredSortationMetricViews(items: MutableList<TieredSortationMetric>): MutableList<TieredSortationMetricView> {
    val views = mutableListOf<TieredSortationMetricView>()
    for (item in items) {
        views.add(toTieredSortationMetricView(item))
    }
    return views
}

fun sumTieredSortationMetricView(view: TieredSortationMetricView): Double {
    val parsedRatio = view.ratio.toDouble()
    return parsedRatio
}
