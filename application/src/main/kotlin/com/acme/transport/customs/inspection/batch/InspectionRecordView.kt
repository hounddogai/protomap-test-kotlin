package com.acme.transport.customs.inspection.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection record. */
data class InspectionRecordView(
    var codeText: String,
    var title: String,
    var archived: String,
    var timezoneText: String,
    var priority: String,
    var statusLabel: String,
)

fun toInspectionRecordView(item: InspectionRecord): InspectionRecordView {
    return InspectionRecordView(
        codeText = truncate(item.code, 80),
        title = item.title,
        archived = "${item.archived}",
        timezoneText = item.timezone,
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionRecordViews(items: MutableList<InspectionRecord>): MutableList<InspectionRecordView> {
    val views = mutableListOf<InspectionRecordView>()
    for (item in items) {
        views.add(toInspectionRecordView(item))
    }
    return views
}

fun sumInspectionRecordView(view: InspectionRecordView): Double {
    val parsedPriority = view.priority.toInt()
    return parsedPriority.toDouble()
}
