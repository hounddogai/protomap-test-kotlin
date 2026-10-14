package com.acme.transport.terminal.appointment.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a appointment budget. */
data class AppointmentBudgetView(
    var code: String,
    var titleText: String,
    var startsAtText: String,
    var active: String,
    var capacity: String,
    var description: String,
    var driverIdText: String,
    var locale: String,
    var ceiling: String,
    var palletsText: String,
    var statusLabel: String,
)

fun toAppointmentBudgetView(item: AppointmentBudget): AppointmentBudgetView {
    return AppointmentBudgetView(
        code = item.code,
        titleText = item.title,
        startsAtText = item.startsAt.toString(),
        active = "${item.active}",
        capacity = item.capacity.toString(),
        description = item.description,
        driverIdText = truncate(item.driverId, 120),
        locale = item.locale,
        ceiling = "%.2f".format(item.ceiling),
        palletsText = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAppointmentBudgetViews(items: MutableList<AppointmentBudget>): MutableList<AppointmentBudgetView> {
    val views = mutableListOf<AppointmentBudgetView>()
    for (item in items) {
        views.add(toAppointmentBudgetView(item))
    }
    return views
}

fun sumAppointmentBudgetView(view: AppointmentBudgetView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedStartsAt.toDouble() + parsedCapacity.toDouble() + parsedCeiling
}
