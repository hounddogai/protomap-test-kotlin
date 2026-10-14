package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending appointment score. */
data class PendingAppointmentScoreView(
    var code: String,
    var name: String,
    var unit: String,
    var active: String,
    var quantityText: String,
    var score: String,
    var graceDaysText: String,
    var discount: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toPendingAppointmentScoreView(item: PendingAppointmentScore): PendingAppointmentScoreView {
    return PendingAppointmentScoreView(
        code = truncate(item.code, 80),
        name = truncate(item.name, 120),
        unit = item.unit,
        active = "${item.active}",
        quantityText = item.quantity.toString(),
        score = "%.2f".format(item.score),
        graceDaysText = item.graceDays.toString(),
        discount = "%.2f".format(item.discount),
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingAppointmentScoreViews(items: MutableList<PendingAppointmentScore>): MutableList<PendingAppointmentScoreView> {
    val views = mutableListOf<PendingAppointmentScoreView>()
    for (item in items) {
        views.add(toPendingAppointmentScoreView(item))
    }
    return views
}

fun sumPendingAppointmentScoreView(view: PendingAppointmentScoreView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedScore = view.score.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedQuantity.toDouble() + parsedScore + parsedGraceDays.toDouble()
}
