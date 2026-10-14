package com.acme.transport.terminal.appointment.adaptiveappointmentmapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy appointment override. */
data class LegacyAppointmentOverrideView(
    var code: String,
    var name: String,
    var checksum: String,
    var approved: String,
    var vehicleIdText: String,
    var depot: String,
    var laneText: String,
    var score: String,
    var statusLabel: String,
)

fun toLegacyAppointmentOverrideView(item: LegacyAppointmentOverride): LegacyAppointmentOverrideView {
    return LegacyAppointmentOverrideView(
        code = item.code,
        name = item.name,
        checksum = truncate(item.checksum, 40),
        approved = "${item.approved}",
        vehicleIdText = item.vehicleId,
        depot = item.depot,
        laneText = item.lane,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyAppointmentOverrideViews(items: MutableList<LegacyAppointmentOverride>): MutableList<LegacyAppointmentOverrideView> {
    val views = mutableListOf<LegacyAppointmentOverrideView>()
    for (item in items) {
        views.add(toLegacyAppointmentOverrideView(item))
    }
    return views
}

fun sumLegacyAppointmentOverrideView(view: LegacyAppointmentOverrideView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
