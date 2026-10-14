package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending inspection override. */
data class PendingInspectionOverrideView(
    var codeText: String,
    var title: String,
    var summary: String,
    var revision: String,
    var limit: String,
    var name: String,
    var quantityText: String,
    var trailerIdText: String,
    var sequence: String,
    var statusLabel: String,
)

fun toPendingInspectionOverrideView(item: PendingInspectionOverride): PendingInspectionOverrideView {
    return PendingInspectionOverrideView(
        codeText = truncate(item.code, 40),
        title = item.title,
        summary = item.summary,
        revision = item.revision.toString(),
        limit = item.limit.toString(),
        name = truncate(item.name, 40),
        quantityText = item.quantity.toString(),
        trailerIdText = item.trailerId,
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingInspectionOverrideViews(items: MutableList<PendingInspectionOverride>): MutableList<PendingInspectionOverrideView> {
    val views = mutableListOf<PendingInspectionOverrideView>()
    for (item in items) {
        views.add(toPendingInspectionOverrideView(item))
    }
    return views
}

fun sumPendingInspectionOverrideView(view: PendingInspectionOverrideView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedQuantity = view.quantityText.toInt()
    return parsedRevision.toDouble() + parsedLimit.toDouble() + parsedQuantity.toDouble()
}
