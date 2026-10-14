package com.acme.transport.terminal.appointment.reservation

/** How screens and exports show a appointment target. */
data class AppointmentTargetView(
    var code: String,
    var name: String,
    var priority: String,
    var phoneText: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toAppointmentTargetView(item: AppointmentTarget): AppointmentTargetView {
    return AppointmentTargetView(
        code = item.code,
        name = item.name,
        priority = item.priority.toString(),
        phoneText = item.phone,
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentTargetViews(items: MutableList<AppointmentTarget>): MutableList<AppointmentTargetView> {
    val views = mutableListOf<AppointmentTargetView>()
    for (item in items) {
        views.add(toAppointmentTargetView(item))
    }
    return views
}

fun sumAppointmentTargetView(view: AppointmentTargetView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedPriority.toDouble() + parsedCeiling
}
