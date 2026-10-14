package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global swap metric. */
data class GlobalSwapMetricView(
    var code: String,
    var title: String,
    var weight: String,
    var lane: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toGlobalSwapMetricView(item: GlobalSwapMetric): GlobalSwapMetricView {
    return GlobalSwapMetricView(
        code = item.code,
        title = truncate(item.title, 80),
        weight = "%.2f".format(item.weight),
        lane = item.lane,
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalSwapMetricViews(items: MutableList<GlobalSwapMetric>): MutableList<GlobalSwapMetricView> {
    val views = mutableListOf<GlobalSwapMetricView>()
    for (item in items) {
        views.add(toGlobalSwapMetricView(item))
    }
    return views
}

fun sumGlobalSwapMetricView(view: GlobalSwapMetricView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedWeight + parsedUpdatedAt.toDouble()
}
