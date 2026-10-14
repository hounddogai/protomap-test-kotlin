package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a appointment milestone. */
data class AppointmentMilestoneView(
    var codeText: String,
    var label: String,
    var contactEmail: String,
    var odometer: String,
    var quantityText: String,
    var driverId: String,
    var statusLabel: String,
)

fun toAppointmentMilestoneView(item: AppointmentMilestone): AppointmentMilestoneView {
    return AppointmentMilestoneView(
        codeText = item.code,
        label = item.label,
        contactEmail = truncate(item.contactEmail, 120),
        odometer = item.odometer.toString(),
        quantityText = item.quantity.toString(),
        driverId = truncate(item.driverId, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentMilestoneViews(items: MutableList<AppointmentMilestone>): MutableList<AppointmentMilestoneView> {
    val views = mutableListOf<AppointmentMilestoneView>()
    for (item in items) {
        views.add(toAppointmentMilestoneView(item))
    }
    return views
}

fun sumAppointmentMilestoneView(view: AppointmentMilestoneView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedQuantity = view.quantityText.toInt()
    return parsedOdometer.toDouble() + parsedQuantity.toDouble()
}
