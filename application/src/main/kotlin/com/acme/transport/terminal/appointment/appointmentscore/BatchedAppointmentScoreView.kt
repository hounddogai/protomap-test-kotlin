package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched appointment score. */
data class BatchedAppointmentScoreView(
    var code: String,
    var title: String,
    var discountText: String,
    var timezoneText: String,
    var channel: String,
    var statusLabel: String,
)

fun toBatchedAppointmentScoreView(item: BatchedAppointmentScore): BatchedAppointmentScoreView {
    return BatchedAppointmentScoreView(
        code = item.code,
        title = item.title,
        discountText = "%.2f".format(item.discount),
        timezoneText = truncate(item.timezone, 40),
        channel = truncate(item.channel, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedAppointmentScoreViews(items: MutableList<BatchedAppointmentScore>): MutableList<BatchedAppointmentScoreView> {
    val views = mutableListOf<BatchedAppointmentScoreView>()
    for (item in items) {
        views.add(toBatchedAppointmentScoreView(item))
    }
    return views
}

fun sumBatchedAppointmentScoreView(view: BatchedAppointmentScoreView): Double {
    val parsedDiscount = view.discountText.toDouble()
    return parsedDiscount
}
