package com.acme.transport.network.node.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched node notice. */
data class BatchedNodeNoticeView(
    var code: String,
    var name: String,
    var pinned: String,
    var stopCountText: String,
    var odometer: String,
    var channel: String,
    var label: String,
    var source: String,
    var statusLabel: String,
)

fun toBatchedNodeNoticeView(item: BatchedNodeNotice): BatchedNodeNoticeView {
    return BatchedNodeNoticeView(
        code = truncate(item.code, 64),
        name = truncate(item.name, 64),
        pinned = "${item.pinned}",
        stopCountText = item.stopCount.toString(),
        odometer = item.odometer.toString(),
        channel = item.channel,
        label = item.label,
        source = truncate(item.source, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedNodeNoticeViews(items: MutableList<BatchedNodeNotice>): MutableList<BatchedNodeNoticeView> {
    val views = mutableListOf<BatchedNodeNoticeView>()
    for (item in items) {
        views.add(toBatchedNodeNoticeView(item))
    }
    return views
}

fun sumBatchedNodeNoticeView(view: BatchedNodeNoticeView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedStopCount.toDouble() + parsedOdometer.toDouble()
}
