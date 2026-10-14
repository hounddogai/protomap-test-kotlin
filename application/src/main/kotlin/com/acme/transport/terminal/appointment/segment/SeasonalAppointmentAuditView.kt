package com.acme.transport.terminal.appointment.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal appointment audit. */
data class SeasonalAppointmentAuditView(
    var code: String,
    var label: String,
    var revision: String,
    var score: String,
    var vehicleIdText: String,
    var quantity: String,
    var segment: String,
    var margin: String,
    var capacityText: String,
    var statusLabel: String,
)

fun toSeasonalAppointmentAuditView(item: SeasonalAppointmentAudit): SeasonalAppointmentAuditView {
    return SeasonalAppointmentAuditView(
        code = truncate(item.code, 40),
        label = item.label,
        revision = item.revision.toString(),
        score = "%.2f".format(item.score),
        vehicleIdText = truncate(item.vehicleId, 64),
        quantity = item.quantity.toString(),
        segment = truncate(item.segment, 40),
        margin = "%.2f".format(item.margin),
        capacityText = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalAppointmentAuditViews(items: MutableList<SeasonalAppointmentAudit>): MutableList<SeasonalAppointmentAuditView> {
    val views = mutableListOf<SeasonalAppointmentAuditView>()
    for (item in items) {
        views.add(toSeasonalAppointmentAuditView(item))
    }
    return views
}

fun sumSeasonalAppointmentAuditView(view: SeasonalAppointmentAuditView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedScore = view.score.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedRevision.toDouble() + parsedScore + parsedQuantity.toDouble()
}
