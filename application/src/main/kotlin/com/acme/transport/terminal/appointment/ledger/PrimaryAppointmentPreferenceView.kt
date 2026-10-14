package com.acme.transport.terminal.appointment.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary appointment preference. */
data class PrimaryAppointmentPreferenceView(
    var code: String,
    var labelText: String,
    var phone: String,
    var unit: String,
    var amountText: String,
    var summaryText: String,
    var revision: String,
    var statusLabel: String,
)

fun toPrimaryAppointmentPreferenceView(item: PrimaryAppointmentPreference): PrimaryAppointmentPreferenceView {
    return PrimaryAppointmentPreferenceView(
        code = truncate(item.code, 80),
        labelText = item.label,
        phone = truncate(item.phone, 40),
        unit = truncate(item.unit, 64),
        amountText = "%.2f".format(item.amount),
        summaryText = item.summary,
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryAppointmentPreferenceViews(items: MutableList<PrimaryAppointmentPreference>): MutableList<PrimaryAppointmentPreferenceView> {
    val views = mutableListOf<PrimaryAppointmentPreferenceView>()
    for (item in items) {
        views.add(toPrimaryAppointmentPreferenceView(item))
    }
    return views
}

fun sumPrimaryAppointmentPreferenceView(view: PrimaryAppointmentPreferenceView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedAmount + parsedRevision.toDouble()
}
