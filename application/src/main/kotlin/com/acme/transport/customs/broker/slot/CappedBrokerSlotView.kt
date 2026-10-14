package com.acme.transport.customs.broker.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped broker slot. */
data class CappedBrokerSlotView(
    var codeText: String,
    var name: String,
    var depotText: String,
    var trailerIdText: String,
    var discount: String,
    var notes: String,
    var segmentText: String,
    var priority: String,
    var region: String,
    var statusLabel: String,
)

fun toCappedBrokerSlotView(item: CappedBrokerSlot): CappedBrokerSlotView {
    return CappedBrokerSlotView(
        codeText = truncate(item.code, 64),
        name = item.name,
        depotText = item.depot,
        trailerIdText = item.trailerId,
        discount = "%.2f".format(item.discount),
        notes = item.notes,
        segmentText = item.segment,
        priority = item.priority.toString(),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedBrokerSlotViews(items: MutableList<CappedBrokerSlot>): MutableList<CappedBrokerSlotView> {
    val views = mutableListOf<CappedBrokerSlotView>()
    for (item in items) {
        views.add(toCappedBrokerSlotView(item))
    }
    return views
}

fun sumCappedBrokerSlotView(view: CappedBrokerSlotView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedDiscount + parsedPriority.toDouble()
}
