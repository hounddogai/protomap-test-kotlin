package com.acme.transport.network.node.nodescore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched node forecast. */
data class BatchedNodeForecastView(
    var code: String,
    var titleText: String,
    var shiftCodeText: String,
    var batchSize: String,
    var timezoneText: String,
    var summary: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toBatchedNodeForecastView(item: BatchedNodeForecast): BatchedNodeForecastView {
    return BatchedNodeForecastView(
        code = item.code,
        titleText = truncate(item.title, 80),
        shiftCodeText = truncate(item.shiftCode, 120),
        batchSize = item.batchSize.toString(),
        timezoneText = item.timezone,
        summary = truncate(item.summary, 120),
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedNodeForecastViews(items: MutableList<BatchedNodeForecast>): MutableList<BatchedNodeForecastView> {
    val views = mutableListOf<BatchedNodeForecastView>()
    for (item in items) {
        views.add(toBatchedNodeForecastView(item))
    }
    return views
}

fun sumBatchedNodeForecastView(view: BatchedNodeForecastView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    return parsedBatchSize.toDouble()
}
