package com.acme.transport.terminal.appointment.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual appointment window. */
data class ManualAppointmentWindowView(
    var code: String,
    var label: String,
    var shiftCode: String,
    var attemptsText: String,
    var endsAt: String,
    var archived: String,
    var minimum: String,
    var channel: String,
    var statusLabel: String,
)

fun toManualAppointmentWindowView(item: ManualAppointmentWindow): ManualAppointmentWindowView {
    return ManualAppointmentWindowView(
        code = item.code,
        label = item.label,
        shiftCode = truncate(item.shiftCode, 80),
        attemptsText = item.attempts.toString(),
        endsAt = item.endsAt.toString(),
        archived = "${item.archived}",
        minimum = "%.2f".format(item.minimum),
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualAppointmentWindowViews(items: MutableList<ManualAppointmentWindow>): MutableList<ManualAppointmentWindowView> {
    val views = mutableListOf<ManualAppointmentWindowView>()
    for (item in items) {
        views.add(toManualAppointmentWindowView(item))
    }
    return views
}

fun sumManualAppointmentWindowView(view: ManualAppointmentWindowView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedAttempts.toDouble() + parsedEndsAt.toDouble() + parsedMinimum
}
