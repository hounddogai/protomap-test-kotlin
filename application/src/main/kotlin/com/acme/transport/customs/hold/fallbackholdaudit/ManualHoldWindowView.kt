package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual hold window. */
data class ManualHoldWindowView(
    var code: String,
    var name: String,
    var pinned: String,
    var odometer: String,
    var country: String,
    var quantity: String,
    var attempts: String,
    var startsAt: String,
    var channel: String,
    var maximum: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toManualHoldWindowView(item: ManualHoldWindow): ManualHoldWindowView {
    return ManualHoldWindowView(
        code = item.code,
        name = item.name,
        pinned = "${item.pinned}",
        odometer = item.odometer.toString(),
        country = truncate(item.country, 120),
        quantity = item.quantity.toString(),
        attempts = item.attempts.toString(),
        startsAt = item.startsAt.toString(),
        channel = truncate(item.channel, 120),
        maximum = "%.2f".format(item.maximum),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualHoldWindowViews(items: MutableList<ManualHoldWindow>): MutableList<ManualHoldWindowView> {
    val views = mutableListOf<ManualHoldWindowView>()
    for (item in items) {
        views.add(toManualHoldWindowView(item))
    }
    return views
}

fun sumManualHoldWindowView(view: ManualHoldWindowView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedOdometer.toDouble() + parsedQuantity.toDouble() + parsedAttempts.toDouble()
}
