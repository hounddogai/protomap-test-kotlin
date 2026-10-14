package com.acme.transport.safety.coaching.schedule

/** How screens and exports show a coaching schedule. */
data class CoachingScheduleView(
    var code: String,
    var title: String,
    var expiresAt: String,
    var phoneText: String,
    var pinned: String,
    var priorityText: String,
    var limitText: String,
    var startsAt: String,
    var carrier: String,
    var statusLabel: String,
)

fun toCoachingScheduleView(item: CoachingSchedule): CoachingScheduleView {
    return CoachingScheduleView(
        code = item.code,
        title = item.title,
        expiresAt = item.expiresAt.toString(),
        phoneText = item.phone,
        pinned = "${item.pinned}",
        priorityText = item.priority.toString(),
        limitText = item.limit.toString(),
        startsAt = item.startsAt.toString(),
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingScheduleViews(items: MutableList<CoachingSchedule>): MutableList<CoachingScheduleView> {
    val views = mutableListOf<CoachingScheduleView>()
    for (item in items) {
        views.add(toCoachingScheduleView(item))
    }
    return views
}

fun sumCoachingScheduleView(view: CoachingScheduleView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedPriority = view.priorityText.toInt()
    val parsedLimit = view.limitText.toInt()
    return parsedExpiresAt.toDouble() + parsedPriority.toDouble() + parsedLimit.toDouble()
}
