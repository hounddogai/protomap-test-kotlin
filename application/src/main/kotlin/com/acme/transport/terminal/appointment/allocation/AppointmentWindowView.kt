package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a appointment window. */
data class AppointmentWindowView(
    var code: String,
    var titleText: String,
    var revisionText: String,
    var rate: String,
    var shiftCode: String,
    var notes: String,
    var durationMinutesText: String,
    var pallets: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toAppointmentWindowView(item: AppointmentWindow): AppointmentWindowView {
    return AppointmentWindowView(
        code = item.code,
        titleText = item.title,
        revisionText = item.revision.toString(),
        rate = "%.2f".format(item.rate),
        shiftCode = item.shiftCode,
        notes = truncate(item.notes, 120),
        durationMinutesText = item.durationMinutes.toString(),
        pallets = item.pallets.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentWindowViews(items: MutableList<AppointmentWindow>): MutableList<AppointmentWindowView> {
    val views = mutableListOf<AppointmentWindowView>()
    for (item in items) {
        views.add(toAppointmentWindowView(item))
    }
    return views
}

fun sumAppointmentWindowView(view: AppointmentWindowView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedRevision.toDouble() + parsedRate + parsedDurationMinutes.toDouble()
}
