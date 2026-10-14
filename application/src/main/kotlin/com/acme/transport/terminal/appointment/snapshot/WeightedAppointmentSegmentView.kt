package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted appointment segment. */
data class WeightedAppointmentSegmentView(
    var code: String,
    var titleText: String,
    var priority: String,
    var stopCount: String,
    var contactEmail: String,
    var laneText: String,
    var segmentText: String,
    var channel: String,
    var notes: String,
    var statusLabel: String,
)

fun toWeightedAppointmentSegmentView(item: WeightedAppointmentSegment): WeightedAppointmentSegmentView {
    return WeightedAppointmentSegmentView(
        code = item.code,
        titleText = item.title,
        priority = item.priority.toString(),
        stopCount = item.stopCount.toString(),
        contactEmail = item.contactEmail,
        laneText = truncate(item.lane, 40),
        segmentText = item.segment,
        channel = item.channel,
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedAppointmentSegmentViews(items: MutableList<WeightedAppointmentSegment>): MutableList<WeightedAppointmentSegmentView> {
    val views = mutableListOf<WeightedAppointmentSegmentView>()
    for (item in items) {
        views.add(toWeightedAppointmentSegmentView(item))
    }
    return views
}

fun sumWeightedAppointmentSegmentView(view: WeightedAppointmentSegmentView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedPriority.toDouble() + parsedStopCount.toDouble()
}
