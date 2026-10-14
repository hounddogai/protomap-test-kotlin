package com.acme.transport.terminal.appointment.rollingappointmentvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional appointment milestone. */
data class RegionalAppointmentMilestoneView(
    var codeText: String,
    var title: String,
    var vehicleId: String,
    var revision: String,
    var trailerIdText: String,
    var summary: String,
    var statusLabel: String,
)

fun toRegionalAppointmentMilestoneView(item: RegionalAppointmentMilestone): RegionalAppointmentMilestoneView {
    return RegionalAppointmentMilestoneView(
        codeText = truncate(item.code, 40),
        title = item.title,
        vehicleId = truncate(item.vehicleId, 120),
        revision = item.revision.toString(),
        trailerIdText = item.trailerId,
        summary = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalAppointmentMilestoneViews(items: MutableList<RegionalAppointmentMilestone>): MutableList<RegionalAppointmentMilestoneView> {
    val views = mutableListOf<RegionalAppointmentMilestoneView>()
    for (item in items) {
        views.add(toRegionalAppointmentMilestoneView(item))
    }
    return views
}

fun sumRegionalAppointmentMilestoneView(view: RegionalAppointmentMilestoneView): Double {
    val parsedRevision = view.revision.toInt()
    return parsedRevision.toDouble()
}
