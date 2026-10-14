package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling appointment bundle. */
data class RollingAppointmentBundleView(
    var code: String,
    var label: String,
    var depot: String,
    var score: String,
    var updatedAtText: String,
    var ownerId: String,
    var quantity: String,
    var dock: String,
    var checksum: String,
    var statusLabel: String,
)

fun toRollingAppointmentBundleView(item: RollingAppointmentBundle): RollingAppointmentBundleView {
    return RollingAppointmentBundleView(
        code = truncate(item.code, 40),
        label = item.label,
        depot = item.depot,
        score = "%.2f".format(item.score),
        updatedAtText = item.updatedAt.toString(),
        ownerId = truncate(item.ownerId, 80),
        quantity = item.quantity.toString(),
        dock = item.dock,
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingAppointmentBundleViews(items: MutableList<RollingAppointmentBundle>): MutableList<RollingAppointmentBundleView> {
    val views = mutableListOf<RollingAppointmentBundleView>()
    for (item in items) {
        views.add(toRollingAppointmentBundleView(item))
    }
    return views
}

fun sumRollingAppointmentBundleView(view: RollingAppointmentBundleView): Double {
    val parsedScore = view.score.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedQuantity = view.quantity.toInt()
    return parsedScore + parsedUpdatedAt.toDouble() + parsedQuantity.toDouble()
}
