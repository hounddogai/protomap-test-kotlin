package com.acme.transport.crewing.availability.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global availability slot. */
data class GlobalAvailabilitySlotView(
    var code: String,
    var nameText: String,
    var startsAt: String,
    var label: String,
    var carrier: String,
    var channel: String,
    var rate: String,
    var revisionText: String,
    var maximum: String,
    var dock: String,
    var statusLabel: String,
)

fun toGlobalAvailabilitySlotView(item: GlobalAvailabilitySlot): GlobalAvailabilitySlotView {
    return GlobalAvailabilitySlotView(
        code = truncate(item.code, 80),
        nameText = truncate(item.name, 40),
        startsAt = item.startsAt.toString(),
        label = item.label,
        carrier = item.carrier,
        channel = truncate(item.channel, 80),
        rate = "%.2f".format(item.rate),
        revisionText = item.revision.toString(),
        maximum = "%.2f".format(item.maximum),
        dock = truncate(item.dock, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalAvailabilitySlotViews(items: MutableList<GlobalAvailabilitySlot>): MutableList<GlobalAvailabilitySlotView> {
    val views = mutableListOf<GlobalAvailabilitySlotView>()
    for (item in items) {
        views.add(toGlobalAvailabilitySlotView(item))
    }
    return views
}

fun sumGlobalAvailabilitySlotView(view: GlobalAvailabilitySlotView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedRevision = view.revisionText.toInt()
    return parsedStartsAt.toDouble() + parsedRate + parsedRevision.toDouble()
}
