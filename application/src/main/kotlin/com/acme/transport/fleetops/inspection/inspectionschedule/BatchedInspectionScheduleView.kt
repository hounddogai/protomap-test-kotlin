package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched inspection schedule. */
data class BatchedInspectionScheduleView(
    var code: String,
    var title: String,
    var expiresAt: String,
    var country: String,
    var loadKgText: String,
    var statusLabel: String,
)

fun toBatchedInspectionScheduleView(item: BatchedInspectionSchedule): BatchedInspectionScheduleView {
    return BatchedInspectionScheduleView(
        code = truncate(item.code, 80),
        title = item.title,
        expiresAt = item.expiresAt.toString(),
        country = item.country,
        loadKgText = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedInspectionScheduleViews(items: MutableList<BatchedInspectionSchedule>): MutableList<BatchedInspectionScheduleView> {
    val views = mutableListOf<BatchedInspectionScheduleView>()
    for (item in items) {
        views.add(toBatchedInspectionScheduleView(item))
    }
    return views
}

fun sumBatchedInspectionScheduleView(view: BatchedInspectionScheduleView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedExpiresAt.toDouble() + parsedLoadKg
}
