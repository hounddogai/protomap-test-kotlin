package com.acme.transport.terminal.appointment.adaptiveappointmentmapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict appointment entry. */
data class StrictAppointmentEntryView(
    var code: String,
    var label: String,
    var fuelLiters: String,
    var active: String,
    var sequence: String,
    var stopCount: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toStrictAppointmentEntryView(item: StrictAppointmentEntry): StrictAppointmentEntryView {
    return StrictAppointmentEntryView(
        code = item.code,
        label = truncate(item.label, 80),
        fuelLiters = "%.2f".format(item.fuelLiters),
        active = "${item.active}",
        sequence = item.sequence.toString(),
        stopCount = item.stopCount.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictAppointmentEntryViews(items: MutableList<StrictAppointmentEntry>): MutableList<StrictAppointmentEntryView> {
    val views = mutableListOf<StrictAppointmentEntryView>()
    for (item in items) {
        views.add(toStrictAppointmentEntryView(item))
    }
    return views
}

fun sumStrictAppointmentEntryView(view: StrictAppointmentEntryView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedFuelLiters + parsedSequence.toDouble() + parsedStopCount.toDouble()
}
