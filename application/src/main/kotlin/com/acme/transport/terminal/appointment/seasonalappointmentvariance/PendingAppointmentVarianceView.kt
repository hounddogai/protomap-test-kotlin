package com.acme.transport.terminal.appointment.seasonalappointmentvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending appointment variance. */
data class PendingAppointmentVarianceView(
    var code: String,
    var name: String,
    var category: String,
    var title: String,
    var phoneText: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toPendingAppointmentVarianceView(item: PendingAppointmentVariance): PendingAppointmentVarianceView {
    return PendingAppointmentVarianceView(
        code = truncate(item.code, 120),
        name = item.name,
        category = item.category,
        title = item.title,
        phoneText = truncate(item.phone, 40),
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingAppointmentVarianceViews(items: MutableList<PendingAppointmentVariance>): MutableList<PendingAppointmentVarianceView> {
    val views = mutableListOf<PendingAppointmentVarianceView>()
    for (item in items) {
        views.add(toPendingAppointmentVarianceView(item))
    }
    return views
}

fun sumPendingAppointmentVarianceView(view: PendingAppointmentVarianceView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
