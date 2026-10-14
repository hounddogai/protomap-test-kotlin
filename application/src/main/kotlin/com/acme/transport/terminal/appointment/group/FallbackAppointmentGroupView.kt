package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback appointment group. */
data class FallbackAppointmentGroupView(
    var codeText: String,
    var label: String,
    var fuelLiters: String,
    var attemptsText: String,
    var updatedAt: String,
    var floorValue: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toFallbackAppointmentGroupView(item: FallbackAppointmentGroup): FallbackAppointmentGroupView {
    return FallbackAppointmentGroupView(
        codeText = truncate(item.code, 64),
        label = item.label,
        fuelLiters = "%.2f".format(item.fuelLiters),
        attemptsText = item.attempts.toString(),
        updatedAt = item.updatedAt.toString(),
        floorValue = "%.2f".format(item.floorValue),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackAppointmentGroupViews(items: MutableList<FallbackAppointmentGroup>): MutableList<FallbackAppointmentGroupView> {
    val views = mutableListOf<FallbackAppointmentGroupView>()
    for (item in items) {
        views.add(toFallbackAppointmentGroupView(item))
    }
    return views
}

fun sumFallbackAppointmentGroupView(view: FallbackAppointmentGroupView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedAttempts = view.attemptsText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedFuelLiters + parsedAttempts.toDouble() + parsedUpdatedAt.toDouble()
}
