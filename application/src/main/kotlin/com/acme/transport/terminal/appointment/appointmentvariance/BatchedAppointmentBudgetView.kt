package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched appointment budget. */
data class BatchedAppointmentBudgetView(
    var code: String,
    var title: String,
    var quantity: String,
    var currency: String,
    var source: String,
    var statusLabel: String,
)

fun toBatchedAppointmentBudgetView(item: BatchedAppointmentBudget): BatchedAppointmentBudgetView {
    return BatchedAppointmentBudgetView(
        code = item.code,
        title = item.title,
        quantity = item.quantity.toString(),
        currency = item.currency,
        source = truncate(item.source, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedAppointmentBudgetViews(items: MutableList<BatchedAppointmentBudget>): MutableList<BatchedAppointmentBudgetView> {
    val views = mutableListOf<BatchedAppointmentBudgetView>()
    for (item in items) {
        views.add(toBatchedAppointmentBudgetView(item))
    }
    return views
}

fun sumBatchedAppointmentBudgetView(view: BatchedAppointmentBudgetView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
