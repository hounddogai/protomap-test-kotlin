package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire slot. */
data class TireSlotView(
    var codeText: String,
    var labelText: String,
    var marginText: String,
    var timezone: String,
    var stopCount: String,
    var maximum: String,
    var windowDays: String,
    var odometerText: String,
    var description: String,
    var country: String,
    var amount: String,
    var statusLabel: String,
)

fun toTireSlotView(item: TireSlot): TireSlotView {
    return TireSlotView(
        codeText = truncate(item.code, 120),
        labelText = truncate(item.label, 64),
        marginText = "%.2f".format(item.margin),
        timezone = item.timezone,
        stopCount = item.stopCount.toString(),
        maximum = "%.2f".format(item.maximum),
        windowDays = item.windowDays.toString(),
        odometerText = item.odometer.toString(),
        description = truncate(item.description, 120),
        country = item.country,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireSlotViews(items: MutableList<TireSlot>): MutableList<TireSlotView> {
    val views = mutableListOf<TireSlotView>()
    for (item in items) {
        views.add(toTireSlotView(item))
    }
    return views
}

fun sumTireSlotView(view: TireSlotView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedMargin + parsedStopCount.toDouble() + parsedMaximum
}
