package com.acme.transport.terminal.appointment.ledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal appointment override. */
data class SeasonalAppointmentOverrideView(
    var code: String,
    var label: String,
    var trailerIdText: String,
    var ownerIdText: String,
    var dock: String,
    var currencyText: String,
    var amount: String,
    var statusLabel: String,
)

fun toSeasonalAppointmentOverrideView(item: SeasonalAppointmentOverride): SeasonalAppointmentOverrideView {
    return SeasonalAppointmentOverrideView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 120),
        trailerIdText = truncate(item.trailerId, 120),
        ownerIdText = truncate(item.ownerId, 64),
        dock = item.dock,
        currencyText = item.currency,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalAppointmentOverrideViews(items: MutableList<SeasonalAppointmentOverride>): MutableList<SeasonalAppointmentOverrideView> {
    val views = mutableListOf<SeasonalAppointmentOverrideView>()
    for (item in items) {
        views.add(toSeasonalAppointmentOverrideView(item))
    }
    return views
}

fun sumSeasonalAppointmentOverrideView(view: SeasonalAppointmentOverrideView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
