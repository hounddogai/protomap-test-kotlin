package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a appointment assignment. */
data class AppointmentAssignmentView(
    var code: String,
    var name: String,
    var phone: String,
    var notes: String,
    var capacityText: String,
    var description: String,
    var statusLabel: String,
)

fun toAppointmentAssignmentView(item: AppointmentAssignment): AppointmentAssignmentView {
    return AppointmentAssignmentView(
        code = item.code,
        name = item.name,
        phone = truncate(item.phone, 64),
        notes = item.notes,
        capacityText = item.capacity.toString(),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentAssignmentViews(items: MutableList<AppointmentAssignment>): MutableList<AppointmentAssignmentView> {
    val views = mutableListOf<AppointmentAssignmentView>()
    for (item in items) {
        views.add(toAppointmentAssignmentView(item))
    }
    return views
}

fun sumAppointmentAssignmentView(view: AppointmentAssignmentView): Double {
    val parsedCapacity = view.capacityText.toInt()
    return parsedCapacity.toDouble()
}
