package com.acme.transport.customs.hold.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped hold override. */
data class CappedHoldOverrideView(
    var code: String,
    var label: String,
    var notes: String,
    var stopCount: String,
    var quantity: String,
    var segment: String,
    var revision: String,
    var capacityText: String,
    var statusLabel: String,
)

fun toCappedHoldOverrideView(item: CappedHoldOverride): CappedHoldOverrideView {
    return CappedHoldOverrideView(
        code = truncate(item.code, 64),
        label = item.label,
        notes = item.notes,
        stopCount = item.stopCount.toString(),
        quantity = item.quantity.toString(),
        segment = truncate(item.segment, 40),
        revision = item.revision.toString(),
        capacityText = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedHoldOverrideViews(items: MutableList<CappedHoldOverride>): MutableList<CappedHoldOverrideView> {
    val views = mutableListOf<CappedHoldOverrideView>()
    for (item in items) {
        views.add(toCappedHoldOverrideView(item))
    }
    return views
}

fun sumCappedHoldOverrideView(view: CappedHoldOverrideView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedStopCount.toDouble() + parsedQuantity.toDouble() + parsedRevision.toDouble()
}
