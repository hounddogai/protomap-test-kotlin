package com.acme.transport.terminal.appointment.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched appointment draft. */
data class BatchedAppointmentDraftView(
    var code: String,
    var name: String,
    var threshold: String,
    var summaryText: String,
    var reviewerText: String,
    var updatedAt: String,
    var shiftCodeText: String,
    var activeText: String,
    var label: String,
    var statusLabel: String,
)

fun toBatchedAppointmentDraftView(item: BatchedAppointmentDraft): BatchedAppointmentDraftView {
    return BatchedAppointmentDraftView(
        code = truncate(item.code, 120),
        name = item.name,
        threshold = "%.2f".format(item.threshold),
        summaryText = item.summary,
        reviewerText = item.reviewer,
        updatedAt = item.updatedAt.toString(),
        shiftCodeText = truncate(item.shiftCode, 40),
        activeText = "${item.active}",
        label = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedAppointmentDraftViews(items: MutableList<BatchedAppointmentDraft>): MutableList<BatchedAppointmentDraftView> {
    val views = mutableListOf<BatchedAppointmentDraftView>()
    for (item in items) {
        views.add(toBatchedAppointmentDraftView(item))
    }
    return views
}

fun sumBatchedAppointmentDraftView(view: BatchedAppointmentDraftView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedThreshold + parsedUpdatedAt.toDouble()
}
