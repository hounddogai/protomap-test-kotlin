package com.acme.transport.terminal.appointment.appointmentmapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary appointment cycle. */
data class PrimaryAppointmentCycleView(
    var codeText: String,
    var label: String,
    var vehicleId: String,
    var revisionText: String,
    var updatedAt: String,
    var segment: String,
    var rateText: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toPrimaryAppointmentCycleView(item: PrimaryAppointmentCycle): PrimaryAppointmentCycleView {
    return PrimaryAppointmentCycleView(
        codeText = item.code,
        label = truncate(item.label, 64),
        vehicleId = item.vehicleId,
        revisionText = item.revision.toString(),
        updatedAt = item.updatedAt.toString(),
        segment = truncate(item.segment, 64),
        rateText = "%.2f".format(item.rate),
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryAppointmentCycleViews(items: MutableList<PrimaryAppointmentCycle>): MutableList<PrimaryAppointmentCycleView> {
    val views = mutableListOf<PrimaryAppointmentCycleView>()
    for (item in items) {
        views.add(toPrimaryAppointmentCycleView(item))
    }
    return views
}

fun sumPrimaryAppointmentCycleView(view: PrimaryAppointmentCycleView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedRate = view.rateText.toDouble()
    return parsedRevision.toDouble() + parsedUpdatedAt.toDouble() + parsedRate
}
