package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling appointment snapshot. */
data class RollingAppointmentSnapshotView(
    var code: String,
    var label: String,
    var region: String,
    var unit: String,
    var channel: String,
    var score: String,
    var statusLabel: String,
)

fun toRollingAppointmentSnapshotView(item: RollingAppointmentSnapshot): RollingAppointmentSnapshotView {
    return RollingAppointmentSnapshotView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 120),
        region = truncate(item.region, 40),
        unit = item.unit,
        channel = item.channel,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingAppointmentSnapshotViews(items: MutableList<RollingAppointmentSnapshot>): MutableList<RollingAppointmentSnapshotView> {
    val views = mutableListOf<RollingAppointmentSnapshotView>()
    for (item in items) {
        views.add(toRollingAppointmentSnapshotView(item))
    }
    return views
}

fun sumRollingAppointmentSnapshotView(view: RollingAppointmentSnapshotView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
