package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary appointment snapshot. */
data class PrimaryAppointmentSnapshotView(
    var codeText: String,
    var label: String,
    var etaMinutesText: String,
    var margin: String,
    var contactEmailText: String,
    var weightText: String,
    var description: String,
    var statusLabel: String,
)

fun toPrimaryAppointmentSnapshotView(item: PrimaryAppointmentSnapshot): PrimaryAppointmentSnapshotView {
    return PrimaryAppointmentSnapshotView(
        codeText = truncate(item.code, 64),
        label = truncate(item.label, 40),
        etaMinutesText = item.etaMinutes.toString(),
        margin = "%.2f".format(item.margin),
        contactEmailText = item.contactEmail,
        weightText = "%.2f".format(item.weight),
        description = truncate(item.description, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryAppointmentSnapshotViews(items: MutableList<PrimaryAppointmentSnapshot>): MutableList<PrimaryAppointmentSnapshotView> {
    val views = mutableListOf<PrimaryAppointmentSnapshotView>()
    for (item in items) {
        views.add(toPrimaryAppointmentSnapshotView(item))
    }
    return views
}

fun sumPrimaryAppointmentSnapshotView(view: PrimaryAppointmentSnapshotView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedWeight = view.weightText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedMargin + parsedWeight
}
