package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict ev charge slot. */
data class StrictEvChargeSlotView(
    var code: String,
    var label: String,
    var nameText: String,
    var startsAt: String,
    var durationMinutesText: String,
    var approved: String,
    var maximumText: String,
    var graceDaysText: String,
    var statusLabel: String,
)

fun toStrictEvChargeSlotView(item: StrictEvChargeSlot): StrictEvChargeSlotView {
    return StrictEvChargeSlotView(
        code = truncate(item.code, 120),
        label = item.label,
        nameText = item.name,
        startsAt = item.startsAt.toString(),
        durationMinutesText = item.durationMinutes.toString(),
        approved = "${item.approved}",
        maximumText = "%.2f".format(item.maximum),
        graceDaysText = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictEvChargeSlotViews(items: MutableList<StrictEvChargeSlot>): MutableList<StrictEvChargeSlotView> {
    val views = mutableListOf<StrictEvChargeSlotView>()
    for (item in items) {
        views.add(toStrictEvChargeSlotView(item))
    }
    return views
}

fun sumStrictEvChargeSlotView(view: StrictEvChargeSlotView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedStartsAt.toDouble() + parsedDurationMinutes.toDouble() + parsedMaximum
}
