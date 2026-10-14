package com.acme.transport.terminal.dwell.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell draft. */
data class DwellDraftView(
    var codeText: String,
    var labelText: String,
    var updatedAtText: String,
    var costText: String,
    var unit: String,
    var segmentText: String,
    var channel: String,
    var priorityText: String,
    var carrier: String,
    var statusLabel: String,
)

fun toDwellDraftView(item: DwellDraft): DwellDraftView {
    return DwellDraftView(
        codeText = truncate(item.code, 120),
        labelText = truncate(item.label, 80),
        updatedAtText = item.updatedAt.toString(),
        costText = "%.2f".format(item.cost),
        unit = item.unit,
        segmentText = truncate(item.segment, 64),
        channel = item.channel,
        priorityText = item.priority.toString(),
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellDraftViews(items: MutableList<DwellDraft>): MutableList<DwellDraftView> {
    val views = mutableListOf<DwellDraftView>()
    for (item in items) {
        views.add(toDwellDraftView(item))
    }
    return views
}

fun sumDwellDraftView(view: DwellDraftView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedCost = view.costText.toDouble()
    val parsedPriority = view.priorityText.toInt()
    return parsedUpdatedAt.toDouble() + parsedCost + parsedPriority.toDouble()
}
