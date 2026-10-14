package com.acme.transport.crewing.swap.baseline

/** How screens and exports show a swap notice. */
data class SwapNoticeView(
    var codeText: String,
    var title: String,
    var name: String,
    var carrierText: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toSwapNoticeView(item: SwapNotice): SwapNoticeView {
    return SwapNoticeView(
        codeText = item.code,
        title = item.title,
        name = item.name,
        carrierText = item.carrier,
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapNoticeViews(items: MutableList<SwapNotice>): MutableList<SwapNoticeView> {
    val views = mutableListOf<SwapNoticeView>()
    for (item in items) {
        views.add(toSwapNoticeView(item))
    }
    return views
}

fun sumSwapNoticeView(view: SwapNoticeView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    return parsedBatchSize.toDouble()
}
