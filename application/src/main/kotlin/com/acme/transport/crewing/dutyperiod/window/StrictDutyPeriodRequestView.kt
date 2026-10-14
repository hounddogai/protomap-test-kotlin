package com.acme.transport.crewing.dutyperiod.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict duty period request. */
data class StrictDutyPeriodRequestView(
    var codeText: String,
    var name: String,
    var timezoneText: String,
    var notesText: String,
    var unitText: String,
    var marginText: String,
    var sequenceText: String,
    var priorityText: String,
    var windowDays: String,
    var vehicleId: String,
    var budget: String,
    var statusLabel: String,
)

fun toStrictDutyPeriodRequestView(item: StrictDutyPeriodRequest): StrictDutyPeriodRequestView {
    return StrictDutyPeriodRequestView(
        codeText = item.code,
        name = item.name,
        timezoneText = truncate(item.timezone, 40),
        notesText = item.notes,
        unitText = item.unit,
        marginText = "%.2f".format(item.margin),
        sequenceText = item.sequence.toString(),
        priorityText = item.priority.toString(),
        windowDays = item.windowDays.toString(),
        vehicleId = item.vehicleId,
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictDutyPeriodRequestViews(items: MutableList<StrictDutyPeriodRequest>): MutableList<StrictDutyPeriodRequestView> {
    val views = mutableListOf<StrictDutyPeriodRequestView>()
    for (item in items) {
        views.add(toStrictDutyPeriodRequestView(item))
    }
    return views
}

fun sumStrictDutyPeriodRequestView(view: StrictDutyPeriodRequestView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    val parsedPriority = view.priorityText.toInt()
    return parsedMargin + parsedSequence.toDouble() + parsedPriority.toDouble()
}
