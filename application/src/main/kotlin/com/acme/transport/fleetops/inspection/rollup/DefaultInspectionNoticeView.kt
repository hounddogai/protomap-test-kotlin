package com.acme.transport.fleetops.inspection.rollup

/** How screens and exports show a default inspection notice. */
data class DefaultInspectionNoticeView(
    var code: String,
    var label: String,
    var segmentText: String,
    var notes: String,
    var approvedText: String,
    var descriptionText: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toDefaultInspectionNoticeView(item: DefaultInspectionNotice): DefaultInspectionNoticeView {
    return DefaultInspectionNoticeView(
        code = item.code,
        label = item.label,
        segmentText = item.segment,
        notes = item.notes,
        approvedText = "${item.approved}",
        descriptionText = item.description,
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultInspectionNoticeViews(items: MutableList<DefaultInspectionNotice>): MutableList<DefaultInspectionNoticeView> {
    val views = mutableListOf<DefaultInspectionNoticeView>()
    for (item in items) {
        views.add(toDefaultInspectionNoticeView(item))
    }
    return views
}

fun sumDefaultInspectionNoticeView(view: DefaultInspectionNoticeView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedDurationMinutes.toDouble()
}
