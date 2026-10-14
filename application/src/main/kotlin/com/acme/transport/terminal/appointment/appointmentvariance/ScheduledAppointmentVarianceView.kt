package com.acme.transport.terminal.appointment.appointmentvariance

/** How screens and exports show a scheduled appointment variance. */
data class ScheduledAppointmentVarianceView(
    var codeText: String,
    var title: String,
    var rate: String,
    var externalRef: String,
    var capacity: String,
    var checksumText: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toScheduledAppointmentVarianceView(item: ScheduledAppointmentVariance): ScheduledAppointmentVarianceView {
    return ScheduledAppointmentVarianceView(
        codeText = item.code,
        title = item.title,
        rate = "%.2f".format(item.rate),
        externalRef = item.externalRef,
        capacity = item.capacity.toString(),
        checksumText = item.checksum,
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledAppointmentVarianceViews(items: MutableList<ScheduledAppointmentVariance>): MutableList<ScheduledAppointmentVarianceView> {
    val views = mutableListOf<ScheduledAppointmentVarianceView>()
    for (item in items) {
        views.add(toScheduledAppointmentVarianceView(item))
    }
    return views
}

fun sumScheduledAppointmentVarianceView(view: ScheduledAppointmentVarianceView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedRate + parsedCapacity.toDouble() + parsedUpdatedAt.toDouble()
}
