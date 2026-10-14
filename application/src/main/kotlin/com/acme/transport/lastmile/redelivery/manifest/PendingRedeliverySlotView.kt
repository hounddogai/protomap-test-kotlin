package com.acme.transport.lastmile.redelivery.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending redelivery slot. */
data class PendingRedeliverySlotView(
    var code: String,
    var name: String,
    var batchSize: String,
    var active: String,
    var margin: String,
    var statusLabel: String,
)

fun toPendingRedeliverySlotView(item: PendingRedeliverySlot): PendingRedeliverySlotView {
    return PendingRedeliverySlotView(
        code = truncate(item.code, 80),
        name = item.name,
        batchSize = item.batchSize.toString(),
        active = "${item.active}",
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingRedeliverySlotViews(items: MutableList<PendingRedeliverySlot>): MutableList<PendingRedeliverySlotView> {
    val views = mutableListOf<PendingRedeliverySlotView>()
    for (item in items) {
        views.add(toPendingRedeliverySlotView(item))
    }
    return views
}

fun sumPendingRedeliverySlotView(view: PendingRedeliverySlotView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedBatchSize.toDouble() + parsedMargin
}
