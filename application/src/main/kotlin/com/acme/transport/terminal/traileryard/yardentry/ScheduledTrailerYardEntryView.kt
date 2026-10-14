package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled trailer yard entry. */
data class ScheduledTrailerYardEntryView(
    var code: String,
    var name: String,
    var vehicleId: String,
    var version: String,
    var carrier: String,
    var statusLabel: String,
)

fun toScheduledTrailerYardEntryView(item: ScheduledTrailerYardEntry): ScheduledTrailerYardEntryView {
    return ScheduledTrailerYardEntryView(
        code = truncate(item.code, 80),
        name = item.name,
        vehicleId = item.vehicleId,
        version = item.version.toString(),
        carrier = truncate(item.carrier, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledTrailerYardEntryViews(items: MutableList<ScheduledTrailerYardEntry>): MutableList<ScheduledTrailerYardEntryView> {
    val views = mutableListOf<ScheduledTrailerYardEntryView>()
    for (item in items) {
        views.add(toScheduledTrailerYardEntryView(item))
    }
    return views
}

fun sumScheduledTrailerYardEntryView(view: ScheduledTrailerYardEntryView): Double {
    val parsedVersion = view.version.toInt()
    return parsedVersion.toDouble()
}
