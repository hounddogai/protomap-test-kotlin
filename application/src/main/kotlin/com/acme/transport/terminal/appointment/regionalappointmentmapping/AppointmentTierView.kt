package com.acme.transport.terminal.appointment.regionalappointmentmapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a appointment tier. */
data class AppointmentTierView(
    var codeText: String,
    var labelText: String,
    var contactEmail: String,
    var floorValueText: String,
    var depotText: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toAppointmentTierView(item: AppointmentTier): AppointmentTierView {
    return AppointmentTierView(
        codeText = item.code,
        labelText = truncate(item.label, 64),
        contactEmail = item.contactEmail,
        floorValueText = "%.2f".format(item.floorValue),
        depotText = item.depot,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentTierViews(items: MutableList<AppointmentTier>): MutableList<AppointmentTierView> {
    val views = mutableListOf<AppointmentTierView>()
    for (item in items) {
        views.add(toAppointmentTierView(item))
    }
    return views
}

fun sumAppointmentTierView(view: AppointmentTierView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedFloorValue + parsedLoadKg
}
