package com.acme.transport.linehaul.trailer.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer target. */
data class TrailerTargetView(
    var code: String,
    var label: String,
    var batchSize: String,
    var dock: String,
    var region: String,
    var depot: String,
    var retriesText: String,
    var title: String,
    var statusLabel: String,
)

fun toTrailerTargetView(item: TrailerTarget): TrailerTargetView {
    return TrailerTargetView(
        code = item.code,
        label = item.label,
        batchSize = item.batchSize.toString(),
        dock = item.dock,
        region = truncate(item.region, 80),
        depot = item.depot,
        retriesText = item.retries.toString(),
        title = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerTargetViews(items: MutableList<TrailerTarget>): MutableList<TrailerTargetView> {
    val views = mutableListOf<TrailerTargetView>()
    for (item in items) {
        views.add(toTrailerTargetView(item))
    }
    return views
}

fun sumTrailerTargetView(view: TrailerTargetView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedRetries = view.retriesText.toInt()
    return parsedBatchSize.toDouble() + parsedRetries.toDouble()
}
