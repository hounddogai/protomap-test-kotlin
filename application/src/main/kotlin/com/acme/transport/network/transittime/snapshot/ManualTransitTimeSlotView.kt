package com.acme.transport.network.transittime.snapshot

/** How screens and exports show a manual transit time slot. */
data class ManualTransitTimeSlotView(
    var code: String,
    var name: String,
    var durationMinutesText: String,
    var loadKg: String,
    var limitText: String,
    var amountText: String,
    var maximum: String,
    var version: String,
    var quantity: String,
    var statusLabel: String,
)

fun toManualTransitTimeSlotView(item: ManualTransitTimeSlot): ManualTransitTimeSlotView {
    return ManualTransitTimeSlotView(
        code = item.code,
        name = item.name,
        durationMinutesText = item.durationMinutes.toString(),
        loadKg = "%.2f".format(item.loadKg),
        limitText = item.limit.toString(),
        amountText = "%.2f".format(item.amount),
        maximum = "%.2f".format(item.maximum),
        version = item.version.toString(),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTransitTimeSlotViews(items: MutableList<ManualTransitTimeSlot>): MutableList<ManualTransitTimeSlotView> {
    val views = mutableListOf<ManualTransitTimeSlotView>()
    for (item in items) {
        views.add(toManualTransitTimeSlotView(item))
    }
    return views
}

fun sumManualTransitTimeSlotView(view: ManualTransitTimeSlotView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedLimit = view.limitText.toInt()
    return parsedDurationMinutes.toDouble() + parsedLoadKg + parsedLimit.toDouble()
}
