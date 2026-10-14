package com.acme.transport.terminal.appointment.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a appointment forecast. */
data class AppointmentForecastView(
    var codeText: String,
    var label: String,
    var approved: String,
    var priority: String,
    var minimum: String,
    var sequence: String,
    var statusLabel: String,
)

fun toAppointmentForecastView(item: AppointmentForecast): AppointmentForecastView {
    return AppointmentForecastView(
        codeText = truncate(item.code, 40),
        label = item.label,
        approved = "${item.approved}",
        priority = item.priority.toString(),
        minimum = "%.2f".format(item.minimum),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentForecastViews(items: MutableList<AppointmentForecast>): MutableList<AppointmentForecastView> {
    val views = mutableListOf<AppointmentForecastView>()
    for (item in items) {
        views.add(toAppointmentForecastView(item))
    }
    return views
}

fun sumAppointmentForecastView(view: AppointmentForecastView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedPriority.toDouble() + parsedMinimum + parsedSequence.toDouble()
}
