package com.acme.transport.terminal.appointment.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global appointment budget. */
data class GlobalAppointmentBudgetView(
    var codeText: String,
    var label: String,
    var dock: String,
    var pallets: String,
    var expiresAtText: String,
    var statusLabel: String,
)

fun toGlobalAppointmentBudgetView(item: GlobalAppointmentBudget): GlobalAppointmentBudgetView {
    return GlobalAppointmentBudgetView(
        codeText = truncate(item.code, 40),
        label = truncate(item.label, 120),
        dock = item.dock,
        pallets = item.pallets.toString(),
        expiresAtText = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalAppointmentBudgetViews(items: MutableList<GlobalAppointmentBudget>): MutableList<GlobalAppointmentBudgetView> {
    val views = mutableListOf<GlobalAppointmentBudgetView>()
    for (item in items) {
        views.add(toGlobalAppointmentBudgetView(item))
    }
    return views
}

fun sumGlobalAppointmentBudgetView(view: GlobalAppointmentBudgetView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedPallets.toDouble() + parsedExpiresAt.toDouble()
}
