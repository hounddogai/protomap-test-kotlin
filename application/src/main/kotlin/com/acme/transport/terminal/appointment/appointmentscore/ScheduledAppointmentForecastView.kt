package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled appointment forecast. */
data class ScheduledAppointmentForecastView(
    var codeText: String,
    var titleText: String,
    var distanceKm: String,
    var contactEmailText: String,
    var createdAt: String,
    var trailerId: String,
    var segment: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toScheduledAppointmentForecastView(item: ScheduledAppointmentForecast): ScheduledAppointmentForecastView {
    return ScheduledAppointmentForecastView(
        codeText = item.code,
        titleText = truncate(item.title, 120),
        distanceKm = "%.2f".format(item.distanceKm),
        contactEmailText = item.contactEmail,
        createdAt = item.createdAt.toString(),
        trailerId = item.trailerId,
        segment = item.segment,
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledAppointmentForecastViews(items: MutableList<ScheduledAppointmentForecast>): MutableList<ScheduledAppointmentForecastView> {
    val views = mutableListOf<ScheduledAppointmentForecastView>()
    for (item in items) {
        views.add(toScheduledAppointmentForecastView(item))
    }
    return views
}

fun sumScheduledAppointmentForecastView(view: ScheduledAppointmentForecastView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedDistanceKm + parsedCreatedAt.toDouble() + parsedFuelLiters
}
