package com.acme.transport.network.node.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient node batch. */
data class LenientNodeBatchView(
    var code: String,
    var nameText: String,
    var quantity: String,
    var sourceText: String,
    var reviewerText: String,
    var localeText: String,
    var notesText: String,
    var statusLabel: String,
)

fun toLenientNodeBatchView(item: LenientNodeBatch): LenientNodeBatchView {
    return LenientNodeBatchView(
        code = item.code,
        nameText = item.name,
        quantity = item.quantity.toString(),
        sourceText = item.source,
        reviewerText = item.reviewer,
        localeText = item.locale,
        notesText = truncate(item.notes, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientNodeBatchViews(items: MutableList<LenientNodeBatch>): MutableList<LenientNodeBatchView> {
    val views = mutableListOf<LenientNodeBatchView>()
    for (item in items) {
        views.add(toLenientNodeBatchView(item))
    }
    return views
}

fun sumLenientNodeBatchView(view: LenientNodeBatchView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
