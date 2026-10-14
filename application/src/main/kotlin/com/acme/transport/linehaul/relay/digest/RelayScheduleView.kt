package com.acme.transport.linehaul.relay.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a relay schedule. */
data class RelayScheduleView(
    var code: String,
    var labelText: String,
    var stopCount: String,
    var capacityText: String,
    var timezone: String,
    var statusLabel: String,
)

fun toRelayScheduleView(item: RelaySchedule): RelayScheduleView {
    return RelayScheduleView(
        code = truncate(item.code, 120),
        labelText = truncate(item.label, 64),
        stopCount = item.stopCount.toString(),
        capacityText = item.capacity.toString(),
        timezone = truncate(item.timezone, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayScheduleViews(items: MutableList<RelaySchedule>): MutableList<RelayScheduleView> {
    val views = mutableListOf<RelayScheduleView>()
    for (item in items) {
        views.add(toRelayScheduleView(item))
    }
    return views
}

fun sumRelayScheduleView(view: RelayScheduleView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedCapacity = view.capacityText.toInt()
    return parsedStopCount.toDouble() + parsedCapacity.toDouble()
}
