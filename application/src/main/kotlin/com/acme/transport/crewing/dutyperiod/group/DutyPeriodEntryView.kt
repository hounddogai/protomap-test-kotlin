package com.acme.transport.crewing.dutyperiod.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a duty period entry. */
data class DutyPeriodEntryView(
    var codeText: String,
    var label: String,
    var summaryText: String,
    var visibleText: String,
    var revision: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toDutyPeriodEntryView(item: DutyPeriodEntry): DutyPeriodEntryView {
    return DutyPeriodEntryView(
        codeText = truncate(item.code, 120),
        label = truncate(item.label, 64),
        summaryText = truncate(item.summary, 120),
        visibleText = "${item.visible}",
        revision = item.revision.toString(),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodEntryViews(items: MutableList<DutyPeriodEntry>): MutableList<DutyPeriodEntryView> {
    val views = mutableListOf<DutyPeriodEntryView>()
    for (item in items) {
        views.add(toDutyPeriodEntryView(item))
    }
    return views
}

fun sumDutyPeriodEntryView(view: DutyPeriodEntryView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedRevision.toDouble() + parsedFloorValue
}
