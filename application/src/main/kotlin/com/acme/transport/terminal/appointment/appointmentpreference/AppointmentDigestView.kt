package com.acme.transport.terminal.appointment.appointmentpreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a appointment digest. */
data class AppointmentDigestView(
    var code: String,
    var name: String,
    var sequence: String,
    var vehicleId: String,
    var country: String,
    var statusLabel: String,
)

fun toAppointmentDigestView(item: AppointmentDigest): AppointmentDigestView {
    return AppointmentDigestView(
        code = item.code,
        name = item.name,
        sequence = item.sequence.toString(),
        vehicleId = truncate(item.vehicleId, 120),
        country = truncate(item.country, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentDigestViews(items: MutableList<AppointmentDigest>): MutableList<AppointmentDigestView> {
    val views = mutableListOf<AppointmentDigestView>()
    for (item in items) {
        views.add(toAppointmentDigestView(item))
    }
    return views
}

fun sumAppointmentDigestView(view: AppointmentDigestView): Double {
    val parsedSequence = view.sequence.toInt()
    return parsedSequence.toDouble()
}
