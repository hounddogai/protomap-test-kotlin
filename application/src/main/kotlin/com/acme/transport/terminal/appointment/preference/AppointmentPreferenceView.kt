package com.acme.transport.terminal.appointment.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a appointment preference. */
data class AppointmentPreferenceView(
    var code: String,
    var label: String,
    var phoneText: String,
    var channel: String,
    var weight: String,
    var budget: String,
    var statusLabel: String,
)

fun toAppointmentPreferenceView(item: AppointmentPreference): AppointmentPreferenceView {
    return AppointmentPreferenceView(
        code = item.code,
        label = item.label,
        phoneText = truncate(item.phone, 120),
        channel = item.channel,
        weight = "%.2f".format(item.weight),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentPreferenceViews(items: MutableList<AppointmentPreference>): MutableList<AppointmentPreferenceView> {
    val views = mutableListOf<AppointmentPreferenceView>()
    for (item in items) {
        views.add(toAppointmentPreferenceView(item))
    }
    return views
}

fun sumAppointmentPreferenceView(view: AppointmentPreferenceView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedWeight + parsedBudget
}
