package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a appointment ledger. */
data class AppointmentLedgerView(
    var code: String,
    var name: String,
    var country: String,
    var version: String,
    var budget: String,
    var statusLabel: String,
)

fun toAppointmentLedgerView(item: AppointmentLedger): AppointmentLedgerView {
    return AppointmentLedgerView(
        code = item.code,
        name = item.name,
        country = truncate(item.country, 64),
        version = item.version.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentLedgerViews(items: MutableList<AppointmentLedger>): MutableList<AppointmentLedgerView> {
    val views = mutableListOf<AppointmentLedgerView>()
    for (item in items) {
        views.add(toAppointmentLedgerView(item))
    }
    return views
}

fun sumAppointmentLedgerView(view: AppointmentLedgerView): Double {
    val parsedVersion = view.version.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedVersion.toDouble() + parsedBudget
}
