package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled near miss schedule. */
data class ScheduledNearMissScheduleView(
    var code: String,
    var labelText: String,
    var shiftCodeText: String,
    var segment: String,
    var name: String,
    var stopCountText: String,
    var endsAt: String,
    var sequence: String,
    var quantity: String,
    var channelText: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toScheduledNearMissScheduleView(item: ScheduledNearMissSchedule): ScheduledNearMissScheduleView {
    return ScheduledNearMissScheduleView(
        code = item.code,
        labelText = item.label,
        shiftCodeText = item.shiftCode,
        segment = item.segment,
        name = item.name,
        stopCountText = item.stopCount.toString(),
        endsAt = item.endsAt.toString(),
        sequence = item.sequence.toString(),
        quantity = item.quantity.toString(),
        channelText = truncate(item.channel, 120),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledNearMissScheduleViews(items: MutableList<ScheduledNearMissSchedule>): MutableList<ScheduledNearMissScheduleView> {
    val views = mutableListOf<ScheduledNearMissScheduleView>()
    for (item in items) {
        views.add(toScheduledNearMissScheduleView(item))
    }
    return views
}

fun sumScheduledNearMissScheduleView(view: ScheduledNearMissScheduleView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedStopCount.toDouble() + parsedEndsAt.toDouble() + parsedSequence.toDouble()
}
