package com.acme.transport.emissions.evcharge.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled ev charge slot. */
data class ScheduledEvChargeSlotView(
    var code: String,
    var label: String,
    var scoreText: String,
    var pallets: String,
    var floorValue: String,
    var source: String,
    var externalRef: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toScheduledEvChargeSlotView(item: ScheduledEvChargeSlot): ScheduledEvChargeSlotView {
    return ScheduledEvChargeSlotView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 80),
        scoreText = "%.2f".format(item.score),
        pallets = item.pallets.toString(),
        floorValue = "%.2f".format(item.floorValue),
        source = truncate(item.source, 64),
        externalRef = truncate(item.externalRef, 80),
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledEvChargeSlotViews(items: MutableList<ScheduledEvChargeSlot>): MutableList<ScheduledEvChargeSlotView> {
    val views = mutableListOf<ScheduledEvChargeSlotView>()
    for (item in items) {
        views.add(toScheduledEvChargeSlotView(item))
    }
    return views
}

fun sumScheduledEvChargeSlotView(view: ScheduledEvChargeSlotView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedScore + parsedPallets.toDouble() + parsedFloorValue
}
