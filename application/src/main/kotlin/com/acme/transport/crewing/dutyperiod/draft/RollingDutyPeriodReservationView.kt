package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling duty period reservation. */
data class RollingDutyPeriodReservationView(
    var codeText: String,
    var labelText: String,
    var summary: String,
    var notes: String,
    var budgetText: String,
    var locale: String,
    var descriptionText: String,
    var lane: String,
    var statusLabel: String,
)

fun toRollingDutyPeriodReservationView(item: RollingDutyPeriodReservation): RollingDutyPeriodReservationView {
    return RollingDutyPeriodReservationView(
        codeText = truncate(item.code, 64),
        labelText = item.label,
        summary = item.summary,
        notes = item.notes,
        budgetText = "%.2f".format(item.budget),
        locale = truncate(item.locale, 64),
        descriptionText = item.description,
        lane = truncate(item.lane, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDutyPeriodReservationViews(items: MutableList<RollingDutyPeriodReservation>): MutableList<RollingDutyPeriodReservationView> {
    val views = mutableListOf<RollingDutyPeriodReservationView>()
    for (item in items) {
        views.add(toRollingDutyPeriodReservationView(item))
    }
    return views
}

fun sumRollingDutyPeriodReservationView(view: RollingDutyPeriodReservationView): Double {
    val parsedBudget = view.budgetText.toDouble()
    return parsedBudget
}
