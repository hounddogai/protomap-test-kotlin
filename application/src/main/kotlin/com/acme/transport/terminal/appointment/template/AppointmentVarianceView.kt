package com.acme.transport.terminal.appointment.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a appointment variance. */
data class AppointmentVarianceView(
    var code: String,
    var title: String,
    var localeText: String,
    var floorValueText: String,
    var scoreText: String,
    var segmentText: String,
    var distanceKm: String,
    var active: String,
    var nameText: String,
    var statusLabel: String,
)

fun toAppointmentVarianceView(item: AppointmentVariance): AppointmentVarianceView {
    return AppointmentVarianceView(
        code = item.code,
        title = truncate(item.title, 80),
        localeText = item.locale,
        floorValueText = "%.2f".format(item.floorValue),
        scoreText = "%.2f".format(item.score),
        segmentText = item.segment,
        distanceKm = "%.2f".format(item.distanceKm),
        active = "${item.active}",
        nameText = truncate(item.name, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentVarianceViews(items: MutableList<AppointmentVariance>): MutableList<AppointmentVarianceView> {
    val views = mutableListOf<AppointmentVarianceView>()
    for (item in items) {
        views.add(toAppointmentVarianceView(item))
    }
    return views
}

fun sumAppointmentVarianceView(view: AppointmentVarianceView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedScore = view.scoreText.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedFloorValue + parsedScore + parsedDistanceKm
}
