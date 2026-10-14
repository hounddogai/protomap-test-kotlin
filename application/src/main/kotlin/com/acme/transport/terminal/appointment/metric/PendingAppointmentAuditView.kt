package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending appointment audit. */
data class PendingAppointmentAuditView(
    var code: String,
    var nameText: String,
    var attempts: String,
    var amount: String,
    var capacity: String,
    var statusLabel: String,
)

fun toPendingAppointmentAuditView(item: PendingAppointmentAudit): PendingAppointmentAuditView {
    return PendingAppointmentAuditView(
        code = truncate(item.code, 120),
        nameText = item.name,
        attempts = item.attempts.toString(),
        amount = "%.2f".format(item.amount),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingAppointmentAuditViews(items: MutableList<PendingAppointmentAudit>): MutableList<PendingAppointmentAuditView> {
    val views = mutableListOf<PendingAppointmentAuditView>()
    for (item in items) {
        views.add(toPendingAppointmentAuditView(item))
    }
    return views
}

fun sumPendingAppointmentAuditView(view: PendingAppointmentAuditView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedAmount = view.amount.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedAttempts.toDouble() + parsedAmount + parsedCapacity.toDouble()
}
