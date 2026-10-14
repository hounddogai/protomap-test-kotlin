package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident proposal. */
data class IncidentProposalView(
    var code: String,
    var label: String,
    var name: String,
    var pinned: String,
    var ceiling: String,
    var notes: String,
    var odometerText: String,
    var summaryText: String,
    var windowDays: String,
    var amount: String,
    var statusLabel: String,
)

fun toIncidentProposalView(item: IncidentProposal): IncidentProposalView {
    return IncidentProposalView(
        code = item.code,
        label = item.label,
        name = item.name,
        pinned = "${item.pinned}",
        ceiling = "%.2f".format(item.ceiling),
        notes = item.notes,
        odometerText = item.odometer.toString(),
        summaryText = truncate(item.summary, 120),
        windowDays = item.windowDays.toString(),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentProposalViews(items: MutableList<IncidentProposal>): MutableList<IncidentProposalView> {
    val views = mutableListOf<IncidentProposalView>()
    for (item in items) {
        views.add(toIncidentProposalView(item))
    }
    return views
}

fun sumIncidentProposalView(view: IncidentProposalView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedOdometer = view.odometerText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedCeiling + parsedOdometer.toDouble() + parsedWindowDays.toDouble()
}
