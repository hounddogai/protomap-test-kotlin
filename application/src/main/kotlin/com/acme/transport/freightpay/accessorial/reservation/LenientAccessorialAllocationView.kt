package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient accessorial allocation. */
data class LenientAccessorialAllocationView(
    var code: String,
    var nameText: String,
    var source: String,
    var quantity: String,
    var timezone: String,
    var statusLabel: String,
)

fun toLenientAccessorialAllocationView(item: LenientAccessorialAllocation): LenientAccessorialAllocationView {
    return LenientAccessorialAllocationView(
        code = truncate(item.code, 80),
        nameText = item.name,
        source = item.source,
        quantity = item.quantity.toString(),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientAccessorialAllocationViews(items: MutableList<LenientAccessorialAllocation>): MutableList<LenientAccessorialAllocationView> {
    val views = mutableListOf<LenientAccessorialAllocationView>()
    for (item in items) {
        views.add(toLenientAccessorialAllocationView(item))
    }
    return views
}

fun sumLenientAccessorialAllocationView(view: LenientAccessorialAllocationView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
