package com.acme.transport.terminal.appointment.variance

/** How screens and exports show a pending appointment cycle. */
data class PendingAppointmentCycleView(
    var codeText: String,
    var nameText: String,
    var odometer: String,
    var vehicleId: String,
    var durationMinutesText: String,
    var currencyText: String,
    var floorValue: String,
    var revision: String,
    var statusLabel: String,
)

fun toPendingAppointmentCycleView(item: PendingAppointmentCycle): PendingAppointmentCycleView {
    return PendingAppointmentCycleView(
        codeText = item.code,
        nameText = item.name,
        odometer = item.odometer.toString(),
        vehicleId = item.vehicleId,
        durationMinutesText = item.durationMinutes.toString(),
        currencyText = item.currency,
        floorValue = "%.2f".format(item.floorValue),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingAppointmentCycleViews(items: MutableList<PendingAppointmentCycle>): MutableList<PendingAppointmentCycleView> {
    val views = mutableListOf<PendingAppointmentCycleView>()
    for (item in items) {
        views.add(toPendingAppointmentCycleView(item))
    }
    return views
}

fun sumPendingAppointmentCycleView(view: PendingAppointmentCycleView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedOdometer.toDouble() + parsedDurationMinutes.toDouble() + parsedFloorValue
}
