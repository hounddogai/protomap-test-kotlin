package com.acme.transport.terminal.appointment.window

/** How screens and exports show a appointment rule. */
data class AppointmentRuleView(
    var code: String,
    var name: String,
    var weightText: String,
    var attempts: String,
    var graceDaysText: String,
    var driverId: String,
    var windowDaysText: String,
    var margin: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toAppointmentRuleView(item: AppointmentRule): AppointmentRuleView {
    return AppointmentRuleView(
        code = item.code,
        name = item.name,
        weightText = "%.2f".format(item.weight),
        attempts = item.attempts.toString(),
        graceDaysText = item.graceDays.toString(),
        driverId = item.driverId,
        windowDaysText = item.windowDays.toString(),
        margin = "%.2f".format(item.margin),
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentRuleViews(items: MutableList<AppointmentRule>): MutableList<AppointmentRuleView> {
    val views = mutableListOf<AppointmentRuleView>()
    for (item in items) {
        views.add(toAppointmentRuleView(item))
    }
    return views
}

fun sumAppointmentRuleView(view: AppointmentRuleView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedAttempts = view.attempts.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedWeight + parsedAttempts.toDouble() + parsedGraceDays.toDouble()
}
