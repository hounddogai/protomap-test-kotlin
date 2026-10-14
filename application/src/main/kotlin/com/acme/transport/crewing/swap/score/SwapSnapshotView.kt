package com.acme.transport.crewing.swap.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a swap snapshot. */
data class SwapSnapshotView(
    var code: String,
    var title: String,
    var name: String,
    var country: String,
    var carrierText: String,
    var amount: String,
    var statusLabel: String,
)

fun toSwapSnapshotView(item: SwapSnapshot): SwapSnapshotView {
    return SwapSnapshotView(
        code = truncate(item.code, 80),
        title = item.title,
        name = item.name,
        country = truncate(item.country, 120),
        carrierText = item.carrier,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapSnapshotViews(items: MutableList<SwapSnapshot>): MutableList<SwapSnapshotView> {
    val views = mutableListOf<SwapSnapshotView>()
    for (item in items) {
        views.add(toSwapSnapshotView(item))
    }
    return views
}

fun sumSwapSnapshotView(view: SwapSnapshotView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
