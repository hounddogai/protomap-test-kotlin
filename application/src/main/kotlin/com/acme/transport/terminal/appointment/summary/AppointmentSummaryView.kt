package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a appointment summary. */
data class AppointmentSummaryView(
    var code: String,
    var name: String,
    var channel: String,
    var priorityText: String,
    var phoneText: String,
    var attempts: String,
    var descriptionText: String,
    var revision: String,
    var statusLabel: String,
)

fun toAppointmentSummaryView(item: AppointmentSummary): AppointmentSummaryView {
    return AppointmentSummaryView(
        code = item.code,
        name = item.name,
        channel = item.channel,
        priorityText = item.priority.toString(),
        phoneText = truncate(item.phone, 64),
        attempts = item.attempts.toString(),
        descriptionText = item.description,
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentSummaryViews(items: MutableList<AppointmentSummary>): MutableList<AppointmentSummaryView> {
    val views = mutableListOf<AppointmentSummaryView>()
    for (item in items) {
        views.add(toAppointmentSummaryView(item))
    }
    return views
}

fun sumAppointmentSummaryView(view: AppointmentSummaryView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedPriority.toDouble() + parsedAttempts.toDouble() + parsedRevision.toDouble()
}
