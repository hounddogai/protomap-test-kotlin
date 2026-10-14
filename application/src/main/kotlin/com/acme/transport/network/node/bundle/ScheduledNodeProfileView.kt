package com.acme.transport.network.node.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled node profile. */
data class ScheduledNodeProfileView(
    var code: String,
    var labelText: String,
    var locale: String,
    var quantityText: String,
    var reviewer: String,
    var revision: String,
    var trailerIdText: String,
    var segment: String,
    var attemptsText: String,
    var statusLabel: String,
)

fun toScheduledNodeProfileView(item: ScheduledNodeProfile): ScheduledNodeProfileView {
    return ScheduledNodeProfileView(
        code = item.code,
        labelText = truncate(item.label, 40),
        locale = truncate(item.locale, 40),
        quantityText = item.quantity.toString(),
        reviewer = item.reviewer,
        revision = item.revision.toString(),
        trailerIdText = item.trailerId,
        segment = truncate(item.segment, 120),
        attemptsText = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledNodeProfileViews(items: MutableList<ScheduledNodeProfile>): MutableList<ScheduledNodeProfileView> {
    val views = mutableListOf<ScheduledNodeProfileView>()
    for (item in items) {
        views.add(toScheduledNodeProfileView(item))
    }
    return views
}

fun sumScheduledNodeProfileView(view: ScheduledNodeProfileView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedQuantity.toDouble() + parsedRevision.toDouble() + parsedAttempts.toDouble()
}
