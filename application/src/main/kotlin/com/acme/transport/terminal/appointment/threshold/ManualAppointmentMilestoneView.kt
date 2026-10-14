package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual appointment milestone. */
data class ManualAppointmentMilestoneView(
    var code: String,
    var titleText: String,
    var fuelLiters: String,
    var updatedAtText: String,
    var score: String,
    var startsAt: String,
    var version: String,
    var statusLabel: String,
)

fun toManualAppointmentMilestoneView(item: ManualAppointmentMilestone): ManualAppointmentMilestoneView {
    return ManualAppointmentMilestoneView(
        code = truncate(item.code, 120),
        titleText = item.title,
        fuelLiters = "%.2f".format(item.fuelLiters),
        updatedAtText = item.updatedAt.toString(),
        score = "%.2f".format(item.score),
        startsAt = item.startsAt.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualAppointmentMilestoneViews(items: MutableList<ManualAppointmentMilestone>): MutableList<ManualAppointmentMilestoneView> {
    val views = mutableListOf<ManualAppointmentMilestoneView>()
    for (item in items) {
        views.add(toManualAppointmentMilestoneView(item))
    }
    return views
}

fun sumManualAppointmentMilestoneView(view: ManualAppointmentMilestoneView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedScore = view.score.toDouble()
    return parsedFuelLiters + parsedUpdatedAt.toDouble() + parsedScore
}
