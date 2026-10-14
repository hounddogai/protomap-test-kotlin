package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive appointment cycle. */
data class AdaptiveAppointmentCycleView(
    var code: String,
    var label: String,
    var vehicleId: String,
    var visible: String,
    var notes: String,
    var countryText: String,
    var startsAt: String,
    var archived: String,
    var statusLabel: String,
)

fun toAdaptiveAppointmentCycleView(item: AdaptiveAppointmentCycle): AdaptiveAppointmentCycleView {
    return AdaptiveAppointmentCycleView(
        code = item.code,
        label = truncate(item.label, 80),
        vehicleId = item.vehicleId,
        visible = "${item.visible}",
        notes = item.notes,
        countryText = item.country,
        startsAt = item.startsAt.toString(),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveAppointmentCycleViews(items: MutableList<AdaptiveAppointmentCycle>): MutableList<AdaptiveAppointmentCycleView> {
    val views = mutableListOf<AdaptiveAppointmentCycleView>()
    for (item in items) {
        views.add(toAdaptiveAppointmentCycleView(item))
    }
    return views
}

fun sumAdaptiveAppointmentCycleView(view: AdaptiveAppointmentCycleView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    return parsedStartsAt.toDouble()
}
