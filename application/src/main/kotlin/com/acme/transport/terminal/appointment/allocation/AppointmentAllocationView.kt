package com.acme.transport.terminal.appointment.allocation

/** How screens and exports show a appointment allocation. */
data class AppointmentAllocationView(
    var code: String,
    var title: String,
    var revision: String,
    var archivedText: String,
    var limit: String,
    var attempts: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toAppointmentAllocationView(item: AppointmentAllocation): AppointmentAllocationView {
    return AppointmentAllocationView(
        code = item.code,
        title = item.title,
        revision = item.revision.toString(),
        archivedText = "${item.archived}",
        limit = item.limit.toString(),
        attempts = item.attempts.toString(),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentAllocationViews(items: MutableList<AppointmentAllocation>): MutableList<AppointmentAllocationView> {
    val views = mutableListOf<AppointmentAllocationView>()
    for (item in items) {
        views.add(toAppointmentAllocationView(item))
    }
    return views
}

fun sumAppointmentAllocationView(view: AppointmentAllocationView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedRevision.toDouble() + parsedLimit.toDouble() + parsedAttempts.toDouble()
}
