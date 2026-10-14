package com.acme.transport.terminal.appointment.segment

/** How screens and exports show a appointment segment. */
data class AppointmentSegmentView(
    var codeText: String,
    var title: String,
    var unit: String,
    var createdAt: String,
    var maximum: String,
    var threshold: String,
    var durationMinutes: String,
    var carrierText: String,
    var updatedAt: String,
    var windowDaysText: String,
    var ceilingText: String,
    var statusLabel: String,
)

fun toAppointmentSegmentView(item: AppointmentSegment): AppointmentSegmentView {
    return AppointmentSegmentView(
        codeText = item.code,
        title = item.title,
        unit = item.unit,
        createdAt = item.createdAt.toString(),
        maximum = "%.2f".format(item.maximum),
        threshold = "%.2f".format(item.threshold),
        durationMinutes = item.durationMinutes.toString(),
        carrierText = item.carrier,
        updatedAt = item.updatedAt.toString(),
        windowDaysText = item.windowDays.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentSegmentViews(items: MutableList<AppointmentSegment>): MutableList<AppointmentSegmentView> {
    val views = mutableListOf<AppointmentSegmentView>()
    for (item in items) {
        views.add(toAppointmentSegmentView(item))
    }
    return views
}

fun sumAppointmentSegmentView(view: AppointmentSegmentView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedCreatedAt.toDouble() + parsedMaximum + parsedThreshold
}
