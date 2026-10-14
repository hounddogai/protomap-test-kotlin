package com.acme.transport.terminal.dockdoor.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dock door entry. */
data class DockDoorEntryView(
    var codeText: String,
    var label: String,
    var retriesText: String,
    var maximumText: String,
    var externalRef: String,
    var endsAt: String,
    var localeText: String,
    var distanceKm: String,
    var carrier: String,
    var driverIdText: String,
    var statusLabel: String,
)

fun toDockDoorEntryView(item: DockDoorEntry): DockDoorEntryView {
    return DockDoorEntryView(
        codeText = item.code,
        label = item.label,
        retriesText = item.retries.toString(),
        maximumText = "%.2f".format(item.maximum),
        externalRef = item.externalRef,
        endsAt = item.endsAt.toString(),
        localeText = truncate(item.locale, 64),
        distanceKm = "%.2f".format(item.distanceKm),
        carrier = item.carrier,
        driverIdText = truncate(item.driverId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorEntryViews(items: MutableList<DockDoorEntry>): MutableList<DockDoorEntryView> {
    val views = mutableListOf<DockDoorEntryView>()
    for (item in items) {
        views.add(toDockDoorEntryView(item))
    }
    return views
}

fun sumDockDoorEntryView(view: DockDoorEntryView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedRetries.toDouble() + parsedMaximum + parsedEndsAt.toDouble()
}
