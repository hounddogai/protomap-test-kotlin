package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped appointment allocation. */
data class CappedAppointmentAllocationView(
    var code: String,
    var label: String,
    var notesText: String,
    var distanceKm: String,
    var batchSizeText: String,
    var timezone: String,
    var priority: String,
    var maximum: String,
    var revision: String,
    var endsAt: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toCappedAppointmentAllocationView(item: CappedAppointmentAllocation): CappedAppointmentAllocationView {
    return CappedAppointmentAllocationView(
        code = item.code,
        label = truncate(item.label, 80),
        notesText = item.notes,
        distanceKm = "%.2f".format(item.distanceKm),
        batchSizeText = item.batchSize.toString(),
        timezone = item.timezone,
        priority = item.priority.toString(),
        maximum = "%.2f".format(item.maximum),
        revision = item.revision.toString(),
        endsAt = item.endsAt.toString(),
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedAppointmentAllocationViews(items: MutableList<CappedAppointmentAllocation>): MutableList<CappedAppointmentAllocationView> {
    val views = mutableListOf<CappedAppointmentAllocationView>()
    for (item in items) {
        views.add(toCappedAppointmentAllocationView(item))
    }
    return views
}

fun sumCappedAppointmentAllocationView(view: CappedAppointmentAllocationView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedDistanceKm + parsedBatchSize.toDouble() + parsedPriority.toDouble()
}
