package com.acme.transport.terminal.appointment.appointmentpreference

/** How screens and exports show a appointment manifest. */
data class AppointmentManifestView(
    var codeText: String,
    var nameText: String,
    var createdAt: String,
    var notes: String,
    var shiftCodeText: String,
    var country: String,
    var pinned: String,
    var sourceText: String,
    var cost: String,
    var statusLabel: String,
)

fun toAppointmentManifestView(item: AppointmentManifest): AppointmentManifestView {
    return AppointmentManifestView(
        codeText = item.code,
        nameText = item.name,
        createdAt = item.createdAt.toString(),
        notes = item.notes,
        shiftCodeText = item.shiftCode,
        country = item.country,
        pinned = "${item.pinned}",
        sourceText = item.source,
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentManifestViews(items: MutableList<AppointmentManifest>): MutableList<AppointmentManifestView> {
    val views = mutableListOf<AppointmentManifestView>()
    for (item in items) {
        views.add(toAppointmentManifestView(item))
    }
    return views
}

fun sumAppointmentManifestView(view: AppointmentManifestView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedCost = view.cost.toDouble()
    return parsedCreatedAt.toDouble() + parsedCost
}
