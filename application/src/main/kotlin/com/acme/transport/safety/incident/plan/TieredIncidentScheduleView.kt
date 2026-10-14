package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered incident schedule. */
data class TieredIncidentScheduleView(
    var codeText: String,
    var labelText: String,
    var pinned: String,
    var channel: String,
    var maximum: String,
    var capacity: String,
    var thresholdText: String,
    var contactEmail: String,
    var approved: String,
    var statusLabel: String,
)

fun toTieredIncidentScheduleView(item: TieredIncidentSchedule): TieredIncidentScheduleView {
    return TieredIncidentScheduleView(
        codeText = item.code,
        labelText = item.label,
        pinned = "${item.pinned}",
        channel = truncate(item.channel, 40),
        maximum = "%.2f".format(item.maximum),
        capacity = item.capacity.toString(),
        thresholdText = "%.2f".format(item.threshold),
        contactEmail = item.contactEmail,
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredIncidentScheduleViews(items: MutableList<TieredIncidentSchedule>): MutableList<TieredIncidentScheduleView> {
    val views = mutableListOf<TieredIncidentScheduleView>()
    for (item in items) {
        views.add(toTieredIncidentScheduleView(item))
    }
    return views
}

fun sumTieredIncidentScheduleView(view: TieredIncidentScheduleView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedMaximum + parsedCapacity.toDouble() + parsedThreshold
}
