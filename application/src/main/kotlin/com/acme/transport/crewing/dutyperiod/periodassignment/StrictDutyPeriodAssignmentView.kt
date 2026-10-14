package com.acme.transport.crewing.dutyperiod.periodassignment

/** How screens and exports show a strict duty period assignment. */
data class StrictDutyPeriodAssignmentView(
    var codeText: String,
    var title: String,
    var segment: String,
    var sequence: String,
    var odometer: String,
    var driverIdText: String,
    var attemptsText: String,
    var nameText: String,
    var statusLabel: String,
)

fun toStrictDutyPeriodAssignmentView(item: StrictDutyPeriodAssignment): StrictDutyPeriodAssignmentView {
    return StrictDutyPeriodAssignmentView(
        codeText = item.code,
        title = item.title,
        segment = item.segment,
        sequence = item.sequence.toString(),
        odometer = item.odometer.toString(),
        driverIdText = item.driverId,
        attemptsText = item.attempts.toString(),
        nameText = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDutyPeriodAssignmentViews(items: MutableList<StrictDutyPeriodAssignment>): MutableList<StrictDutyPeriodAssignmentView> {
    val views = mutableListOf<StrictDutyPeriodAssignmentView>()
    for (item in items) {
        views.add(toStrictDutyPeriodAssignmentView(item))
    }
    return views
}

fun sumStrictDutyPeriodAssignmentView(view: StrictDutyPeriodAssignmentView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedSequence.toDouble() + parsedOdometer.toDouble() + parsedAttempts.toDouble()
}
