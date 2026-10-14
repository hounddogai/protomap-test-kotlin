package com.acme.transport.safety.cameraevent.evententry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled camera event entry. */
data class ScheduledCameraEventEntryView(
    var code: String,
    var title: String,
    var depot: String,
    var dock: String,
    var countryText: String,
    var quantity: String,
    var routeCode: String,
    var windowDays: String,
    var capacityText: String,
    var statusLabel: String,
)

fun toScheduledCameraEventEntryView(item: ScheduledCameraEventEntry): ScheduledCameraEventEntryView {
    return ScheduledCameraEventEntryView(
        code = truncate(item.code, 120),
        title = item.title,
        depot = item.depot,
        dock = truncate(item.dock, 120),
        countryText = truncate(item.country, 64),
        quantity = item.quantity.toString(),
        routeCode = truncate(item.routeCode, 40),
        windowDays = item.windowDays.toString(),
        capacityText = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledCameraEventEntryViews(items: MutableList<ScheduledCameraEventEntry>): MutableList<ScheduledCameraEventEntryView> {
    val views = mutableListOf<ScheduledCameraEventEntryView>()
    for (item in items) {
        views.add(toScheduledCameraEventEntryView(item))
    }
    return views
}

fun sumScheduledCameraEventEntryView(view: ScheduledCameraEventEntryView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedCapacity = view.capacityText.toInt()
    return parsedQuantity.toDouble() + parsedWindowDays.toDouble() + parsedCapacity.toDouble()
}
