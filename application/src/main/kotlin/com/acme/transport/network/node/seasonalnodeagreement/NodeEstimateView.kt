package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a node estimate. */
data class NodeEstimateView(
    var code: String,
    var label: String,
    var summary: String,
    var ratio: String,
    var loadKgText: String,
    var channelText: String,
    var retriesText: String,
    var margin: String,
    var cost: String,
    var limitText: String,
    var statusLabel: String,
)

fun toNodeEstimateView(item: NodeEstimate): NodeEstimateView {
    return NodeEstimateView(
        code = truncate(item.code, 64),
        label = item.label,
        summary = truncate(item.summary, 120),
        ratio = "%.2f".format(item.ratio),
        loadKgText = "%.2f".format(item.loadKg),
        channelText = item.channel,
        retriesText = item.retries.toString(),
        margin = "%.2f".format(item.margin),
        cost = "%.2f".format(item.cost),
        limitText = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeEstimateViews(items: MutableList<NodeEstimate>): MutableList<NodeEstimateView> {
    val views = mutableListOf<NodeEstimateView>()
    for (item in items) {
        views.add(toNodeEstimateView(item))
    }
    return views
}

fun sumNodeEstimateView(view: NodeEstimateView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedRetries = view.retriesText.toInt()
    return parsedRatio + parsedLoadKg + parsedRetries.toDouble()
}
