package com.acme.transport.crewing.swap.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a swap record. */
data class SwapRecordView(
    var codeText: String,
    var label: String,
    var source: String,
    var reviewer: String,
    var channel: String,
    var quantity: String,
    var statusLabel: String,
)

fun toSwapRecordView(item: SwapRecord): SwapRecordView {
    return SwapRecordView(
        codeText = item.code,
        label = truncate(item.label, 120),
        source = item.source,
        reviewer = item.reviewer,
        channel = item.channel,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapRecordViews(items: MutableList<SwapRecord>): MutableList<SwapRecordView> {
    val views = mutableListOf<SwapRecordView>()
    for (item in items) {
        views.add(toSwapRecordView(item))
    }
    return views
}

fun sumSwapRecordView(view: SwapRecordView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
