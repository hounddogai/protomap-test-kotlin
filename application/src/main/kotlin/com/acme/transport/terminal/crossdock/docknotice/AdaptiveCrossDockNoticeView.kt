package com.acme.transport.terminal.crossdock.docknotice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive cross dock notice. */
data class AdaptiveCrossDockNoticeView(
    var codeText: String,
    var name: String,
    var batchSize: String,
    var sequence: String,
    var quantity: String,
    var active: String,
    var externalRef: String,
    var createdAt: String,
    var ceiling: String,
    var windowDays: String,
    var channel: String,
    var statusLabel: String,
)

fun toAdaptiveCrossDockNoticeView(item: AdaptiveCrossDockNotice): AdaptiveCrossDockNoticeView {
    return AdaptiveCrossDockNoticeView(
        codeText = item.code,
        name = truncate(item.name, 64),
        batchSize = item.batchSize.toString(),
        sequence = item.sequence.toString(),
        quantity = item.quantity.toString(),
        active = "${item.active}",
        externalRef = item.externalRef,
        createdAt = item.createdAt.toString(),
        ceiling = "%.2f".format(item.ceiling),
        windowDays = item.windowDays.toString(),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveCrossDockNoticeViews(items: MutableList<AdaptiveCrossDockNotice>): MutableList<AdaptiveCrossDockNoticeView> {
    val views = mutableListOf<AdaptiveCrossDockNoticeView>()
    for (item in items) {
        views.add(toAdaptiveCrossDockNoticeView(item))
    }
    return views
}

fun sumAdaptiveCrossDockNoticeView(view: AdaptiveCrossDockNoticeView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedBatchSize.toDouble() + parsedSequence.toDouble() + parsedQuantity.toDouble()
}
