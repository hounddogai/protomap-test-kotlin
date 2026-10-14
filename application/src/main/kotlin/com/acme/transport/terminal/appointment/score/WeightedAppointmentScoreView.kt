package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted appointment score. */
data class WeightedAppointmentScoreView(
    var code: String,
    var labelText: String,
    var endsAt: String,
    var expiresAtText: String,
    var odometer: String,
    var checksumText: String,
    var segmentText: String,
    var startsAtText: String,
    var source: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toWeightedAppointmentScoreView(item: WeightedAppointmentScore): WeightedAppointmentScoreView {
    return WeightedAppointmentScoreView(
        code = item.code,
        labelText = item.label,
        endsAt = item.endsAt.toString(),
        expiresAtText = item.expiresAt.toString(),
        odometer = item.odometer.toString(),
        checksumText = item.checksum,
        segmentText = truncate(item.segment, 40),
        startsAtText = item.startsAt.toString(),
        source = truncate(item.source, 80),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedAppointmentScoreViews(items: MutableList<WeightedAppointmentScore>): MutableList<WeightedAppointmentScoreView> {
    val views = mutableListOf<WeightedAppointmentScoreView>()
    for (item in items) {
        views.add(toWeightedAppointmentScoreView(item))
    }
    return views
}

fun sumWeightedAppointmentScoreView(view: WeightedAppointmentScoreView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedEndsAt.toDouble() + parsedExpiresAt.toDouble() + parsedOdometer.toDouble()
}
