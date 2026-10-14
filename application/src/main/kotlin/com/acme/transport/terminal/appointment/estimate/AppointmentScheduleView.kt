package com.acme.transport.terminal.appointment.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a appointment schedule. */
data class AppointmentScheduleView(
    var code: String,
    var name: String,
    var score: String,
    var label: String,
    var trailerIdText: String,
    var priority: String,
    var createdAt: String,
    var segmentText: String,
    var driverIdText: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toAppointmentScheduleView(item: AppointmentSchedule): AppointmentScheduleView {
    return AppointmentScheduleView(
        code = truncate(item.code, 40),
        name = truncate(item.name, 80),
        score = "%.2f".format(item.score),
        label = truncate(item.label, 64),
        trailerIdText = truncate(item.trailerId, 80),
        priority = item.priority.toString(),
        createdAt = item.createdAt.toString(),
        segmentText = item.segment,
        driverIdText = item.driverId,
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentScheduleViews(items: MutableList<AppointmentSchedule>): MutableList<AppointmentScheduleView> {
    val views = mutableListOf<AppointmentScheduleView>()
    for (item in items) {
        views.add(toAppointmentScheduleView(item))
    }
    return views
}

fun sumAppointmentScheduleView(view: AppointmentScheduleView): Double {
    val parsedScore = view.score.toDouble()
    val parsedPriority = view.priority.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedScore + parsedPriority.toDouble() + parsedCreatedAt.toDouble()
}
