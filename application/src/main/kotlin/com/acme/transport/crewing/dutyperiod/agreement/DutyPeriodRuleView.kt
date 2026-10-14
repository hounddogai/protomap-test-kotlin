package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period rule. */
data class DutyPeriodRuleView(
    var code: String,
    var labelText: String,
    var odometerText: String,
    var fuelLiters: String,
    var durationMinutes: String,
    var revisionText: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toDutyPeriodRuleView(item: DutyPeriodRule): DutyPeriodRuleView {
    return DutyPeriodRuleView(
        code = truncate(item.code, 40),
        labelText = item.label,
        odometerText = item.odometer.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        durationMinutes = item.durationMinutes.toString(),
        revisionText = item.revision.toString(),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodRuleViews(items: MutableList<DutyPeriodRule>): MutableList<DutyPeriodRuleView> {
    val views = mutableListOf<DutyPeriodRuleView>()
    for (item in items) {
        views.add(toDutyPeriodRuleView(item))
    }
    return views
}

fun sumDutyPeriodRuleView(view: DutyPeriodRuleView): Double {
    val parsedOdometer = view.odometerText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedOdometer.toDouble() + parsedFuelLiters + parsedDurationMinutes.toDouble()
}
