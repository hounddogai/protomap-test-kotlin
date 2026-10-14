package com.acme.transport.lastmile.redelivery.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient redelivery slot. */
data class LenientRedeliverySlotView(
    var code: String,
    var name: String,
    var sequenceText: String,
    var weight: String,
    var priorityText: String,
    var approved: String,
    var routeCode: String,
    var label: String,
    var rateText: String,
    var channelText: String,
    var depot: String,
    var statusLabel: String,
)

fun toLenientRedeliverySlotView(item: LenientRedeliverySlot): LenientRedeliverySlotView {
    return LenientRedeliverySlotView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 120),
        sequenceText = item.sequence.toString(),
        weight = "%.2f".format(item.weight),
        priorityText = item.priority.toString(),
        approved = "${item.approved}",
        routeCode = item.routeCode,
        label = item.label,
        rateText = "%.2f".format(item.rate),
        channelText = truncate(item.channel, 80),
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientRedeliverySlotViews(items: MutableList<LenientRedeliverySlot>): MutableList<LenientRedeliverySlotView> {
    val views = mutableListOf<LenientRedeliverySlotView>()
    for (item in items) {
        views.add(toLenientRedeliverySlotView(item))
    }
    return views
}

fun sumLenientRedeliverySlotView(view: LenientRedeliverySlotView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedPriority = view.priorityText.toInt()
    return parsedSequence.toDouble() + parsedWeight + parsedPriority.toDouble()
}
