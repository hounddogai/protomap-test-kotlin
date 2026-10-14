package com.acme.transport.terminal.appointment.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending appointment baseline. */
data class PendingAppointmentBaselineView(
    var code: String,
    var name: String,
    var phoneText: String,
    var reviewer: String,
    var floorValue: String,
    var odometer: String,
    var statusLabel: String,
)

fun toPendingAppointmentBaselineView(item: PendingAppointmentBaseline): PendingAppointmentBaselineView {
    return PendingAppointmentBaselineView(
        code = item.code,
        name = item.name,
        phoneText = item.phone,
        reviewer = truncate(item.reviewer, 120),
        floorValue = "%.2f".format(item.floorValue),
        odometer = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingAppointmentBaselineViews(items: MutableList<PendingAppointmentBaseline>): MutableList<PendingAppointmentBaselineView> {
    val views = mutableListOf<PendingAppointmentBaselineView>()
    for (item in items) {
        views.add(toPendingAppointmentBaselineView(item))
    }
    return views
}

fun sumPendingAppointmentBaselineView(view: PendingAppointmentBaselineView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedOdometer = view.odometer.toInt()
    return parsedFloorValue + parsedOdometer.toDouble()
}
