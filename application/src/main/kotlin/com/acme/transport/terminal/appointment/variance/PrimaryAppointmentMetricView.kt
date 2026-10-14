package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary appointment metric. */
data class PrimaryAppointmentMetricView(
    var codeText: String,
    var labelText: String,
    var ownerIdText: String,
    var locale: String,
    var country: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toPrimaryAppointmentMetricView(item: PrimaryAppointmentMetric): PrimaryAppointmentMetricView {
    return PrimaryAppointmentMetricView(
        codeText = item.code,
        labelText = item.label,
        ownerIdText = item.ownerId,
        locale = item.locale,
        country = truncate(item.country, 120),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryAppointmentMetricViews(items: MutableList<PrimaryAppointmentMetric>): MutableList<PrimaryAppointmentMetricView> {
    val views = mutableListOf<PrimaryAppointmentMetricView>()
    for (item in items) {
        views.add(toPrimaryAppointmentMetricView(item))
    }
    return views
}

fun sumPrimaryAppointmentMetricView(view: PrimaryAppointmentMetricView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedDistanceKm
}
